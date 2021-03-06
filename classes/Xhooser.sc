// SHOULD  HAVE COMMON SUPERCLASS WITH TIMECHOOSER
Xhooser {
	var <>noseCone;
	var <>lanes; // COPY - should be careful copy
	var <>chosenLanes; // COPY - should be new
	var <> timeChooser;
	var <> journal ;
	var <> name;
	var <> hasParent;
	var <> outBus;
	var <> myclocks; // COPY - should be new
	    // maye be needed for nesting -NO !! in wrapper!!
	   // but could record an event stream - instance of non det command pattern?
	   // approx 1000 LOC total
	var <> group;  // COPY - should be new
	var <> chosenSynthsHaveBeenFreed = false;

// =====  INITIALIZATION	 =======

init{
	lanes = List.new;
		chosenLanes = List.new;
		journal = List.new;
		hasParent = false;
		name ="Unnamed Chooser";
	}

*new { ^ super.new.init}

cleanUp {
		this.cleanUpClocks;
		this.cleanUpLanes;
		this.cleanUpTimeChooser;
		this.cleanUpRest
	}

cleanUpClocks {
		this.myclocks.do { arg eachClock, i;    eachClock.isNil.not.if{ eachClock.stop}};
		this.myclocks_( nil)
	}

cleanUpLanes {
		this.chosenLanes.do { arg each ;  each.isNil.not.if{ each.cleanUp }} ;
		this.chosenLanes_(nil);
		this.lanes.do { arg each ;  each.isNil.not.if{ each.cleanUp }} ;
		this.lanes_(nil)
	}

cleanUpTimeChooser {
		this.timeChooser.isNil.not.if{ this.timeChooser.cleanUp;                         this.timeChooser_(nil)}
	}

cleanUpRest {
		this.noseCone_ (nil);
		this.journal_(nil);
		this.name.isString.if{this.name_(this.name + "cleaned")};
		this.hasParent_(nil);
		this.outBus_(nil);
		this.group_(nil);
	}




kopy{ var me, nuLanes;
		  /// used in loopablesequecne
		// we just want fresh lanes to get us fresh sample insatcnes
		// to avoid sinadvertent sample instance reuse
		// and wrong node getting killed
		// got to be kopy or will be infinite loop
		// make a copy of me
		//copy my lanes separately using bog standard copy
		// so its kind of  semi deep copy
		// copies will share the samples
		// but samples will get freshly played in new copy
		// so have no synths shared in synth instance variable
		//each play will create a new node ID and a  create & stire new synth
		//the two samples will save synths spereately
		// and so handle swithcing on and off propely (play & free)
		me = this.copy;

		// this.lanes.do { arg each; each.debug(" A lane")};

		nuLanes = (this.lanes.collect{ arg eachLane ; eachLane.kopy}).asList;
		me.lanes_(nuLanes);

		       //this.timeChooser.debug ("original");
		       this.timeChooser.isNil.not.if
	            	{me.timeChooser_(this.timeChooser.kopy)}; //FIX ME!!!!
		       //me.timeChooser.debug ("kopy");

		//loopsSeq needs to set new group here.
		// what about when a loopabel sequ is the thing in the loopable seq??
		// LAnes are not chosen yet  in loopable seq when this is coped
		// clocks are not used in this class
		^ me }

myGroup{ arg g;
		// called by looupable sequecne
		   this.group_(g); // nasty pointless dangerous duplication memo
		this.lanes.do{arg each; each.group(g)}
		   // nb telling samples ie synth defs ie synths not created yet
	}



cleanAllSamples { arg n;
		this.lanes.do { arg eachLane, i;  eachLane.clean;  eachLane.bugFix(n)}
	}

// ====== LANE ADDING & REMOVAL  =========
addLane{
		arg aLane;
		aLane.parentName_(this.name);
		this.lanes.add(aLane);
		// this.lanes.debug("lanes")
	}

//======= Printing  ==========
	printOn { | aStream |
		aStream << "a " << this.class.name << " chosen lanes " <<  this.chosenLanes;
		^aStream}

	journalAsArray {
		^ this.journal.asArray}

	chosenLanesAsArray {
		^ this.chosenLanes.asArray}

	explore{ ^ this.chosenLanesAsArray.inspect}


// ========TESTING  QUERYING & Accessors ========

isChooser {^true}

noseConeIsInfinite{
		^this.noseCone == inf}

noseConeIsZero{
		^this.noseCone == 0}

hasPriorityBoarders {
			^ this.priorityBoarders.size>0}

hasTooManyPriorityBoarders{
		      ^ (this.priorityBoarders.size > noseCone)}

hasTimeChooser{
		// this.timeChooser.debug("time chooser");
		//(this.timeChooser==nil).if{ "timechooser is nil".postln};
		^(this.timeChooser==nil).not
	}

hasActiveTimeChooser{
		//this.hasTimeChooser.if { "has timechooser".postln};
          //this.timeChooser.isActive.if { "timechooser is active".postln};
		^ this.hasTimeChooser.and( {this.timeChooser.isActive});
		}

lanesNotChosenYet{
		this.chosenLanes.isNil.if { "chosenLanes not  list - should not happen".postln; ^ true};
		^this.chosenLanes.isEmpty
	}

allChosenSynths{
		^ this.chosenLanes.collect{ arg eachLane, i;  eachLane.synth}
	}


	allChosenSamples{
		var chosenSamples;
		chosenSamples = this.chosenLanes.collect{ arg eachLane, i;  eachLane.sample};
		// chosenSamples.debug("collecting chosen samples for freeing");
		^chosenSamples
	}
integrityCheck{
		this.lanes.do {arg eachLane, index; eachLane.sample.isNil.if { "SampleBank not loaded".postln}}
	}


chosenLanesThatContainChoosers {
		^this.chosenLanes.select{ arg eachLane, i;  eachLane.sample.isWrapper}
	}


// ======  DERIVED COLLECTIONS AND NUMBERS ============
priorityBoarders {
			^ this.lanes.select({ arg eachLane, index; eachLane.hasInfiniteWeight})   }

finiteWeightedNonZeroLanes{
		^ this.lanes.select({arg eachLane, index ; (eachLane.hasInfiniteWeight.not).and({eachLane.hasZeroWeight.not}) } )   }

nonZeroWeightedLanes {
		var nzwl;
		nzwl = this.lanes.reject({ arg eachLane, index; eachLane.hasZeroWeight});
		^ nzwl  // RETURNS ARRAY - so could not add playable time lane
	}

noOfLanesStillToPick{
			   // only works with finite noseCone - but
		       // nonDeterministicLaneChoice  ensures never gets called if noseCone is infinite
		this.noseConeIsZero.if { ^0};
		^  (this.noseCone -  this.chosenLanes.size)

	                       }
chosenTimeLane {
		this.hasActiveTimeChooser.if (
			{^ this.timeChooser.chosenLane},
			{  "No active time chooser2".postln; ^nil})
		    // what if none chosen yet?
		     // chooseLanes lgorithn ensures never gets called
		     // but good program defensively in case bugs
	}


// ========    CHOOSING LANES  - Helper Methods    =================

	chooseWinnersFromTooManPriorityBoarders	 {
			| pool|
			 pool = this.priorityBoarders.copy;
			 noseCone.do ({ | choice|
				                     choice = pool.choose;
			                         this.chosenLanes.add (choice);
				                     pool.remove(choice)});
			^ this.chosenLanes}

chooseWinnersFromFiniteNonZeroWeightedLanes{
		     // needs this.chosenLanes to have been initialised by
		      // nonDeterministicLaneChoice - should  chosenLanes have been a set
		var finiteNonZeroWeightedPool = this.finiteWeightedNonZeroLanes;
		 var pool = List.new;
		 var poolWeights = List.new;
		var  normalizedWeights;
		var noOfRemainingPicks;

		this.finiteWeightedNonZeroLanes.do({ arg eachLane; pool.add(eachLane);
			                      poolWeights.add(eachLane.weight)}); // initialize pool weights

		 noOfRemainingPicks = this.noOfLanesStillToPick;
		                                                          // overly cautious - probably not needed
	     noOfRemainingPicks.do({  | choice|
	     normalizedWeights = poolWeights.asArray.normalizeSum; //need to normalize
			                                      choice = pool.asArray.wchoose(normalizedWeights);
			                                                    // wchoose only works for arrays
			                                     (choice==nil).if(
			                       {"Not enough non-zero weighted playable lanes for nosecone".postln;
					                                    // maybe adjust one or the other in response?
			                                    });
			                                      chosenLanes.add(choice);
			                                      pool.remove(choice);
			                                       poolWeights = List.new;
			                                              // no need to remove chosen weight
			                                              //- just recreate weights for shrunken pool
			                                       pool.do ({ arg eachLane; poolWeights.add(eachLane.weight)});
			                                         })
			^ this.chosenLanes  }

// ========    CHOOSING LANES  - MAIN  METHODS   =================
nonDeterministicLaneChoice {
     	this.chosenLanes_(List.new);
		// Must be initialised HERE when this is called - not when creating the Xhooser
	   // 6 cases - zero noseCone
	this.noseConeIsZero.if ({ ^chosenLanes; }) ;//and we are out

	// infinite NoseCone
		this.noseConeIsInfinite.if ({
			       ^ this.chosenLanes.addAll(this.nonZeroWeightedLanes) }) ;//and we are out

    // noseCone is not infinite but there are  too many priority boarders...
	this.hasTooManyPriorityBoarders.if({ this.chooseWinnersFromTooManPriorityBoarders;  ^ chosenLanes  });
		// and we are out

	// Chooser does not have too many priority boarders
	//- so if there are any at all should add them all
	// but must go on next  to add any non zero weighted finite ones
	 this.hasPriorityBoarders.if({ chosenLanes.addAll(this.priorityBoarders) ;  });

     //  If no of priority boarders exactly matches number of seats, we are done
	    (this.noOfLanesStillToPick== 0).if({  ^ chosenLanes }) ; //and we are out

	//  make all (or any remaining) choices from from  solely finitely weighted lanes
		 ^ this.chooseWinnersFromFiniteNonZeroWeightedLanes
	         }

choose{
		^ this.chooseLanes}  // needed to let loopableSequence nest cleanly
	                                     // protocol polymorphism


	chooseLanes{
		//this.name.debug("in chooseLanes in Chooser");

		this.cleanLanes; // remove any nils from lists of lanes
		this.hasActiveTimeChooser.if {  this.timeChooser.chooseLane  }; // EXCELLENT
		this.nonDeterministicLaneChoice;
		this.cleanChosenLanes;
		this.hasActiveTimeChooser.if (
			{this.calculateSmartDurationWithChosenTimeLane},  // not needed to play it right - need for sequencing
			{this.calculateSmartDurationWithNoActiveTimeLane}); // not needed to play it right - need for sequencing

		this.chosenTimeLaneIsFullyPlayable.if {this.addPlayableTimeLaneToChosenLanes};
		^ this.chosenLanes
        // Lane not scheduled  or played yet - just chosen
		}

	cleanLanes {
				this.lanes.removeEvery(List[nil]);

	}


	cleanChosenLanes {
				this.chosenLanes.removeEvery(List[nil]);
		       this.chosenLanes.isEmpty.if{ this.chosenLanes.add(Lane.null)};
	}


// =========== PLAYABLE TIME LANES  ========================
	chosenTimeLaneIsFullyPlayable {
		this.hasActiveTimeChooser.not.if { /*this.debug("No active time chooser!"); */^false};
		^ this.timeChooser.chosenLaneIsFullyPlayable
	}

	addPlayableTimeLaneToChosenLanes {
		 var newLane;  //  will this displace one lane from the count? nope
		this.hasActiveTimeChooser.and({this.chosenTimeLaneIsFullyPlayable}).if
		(    newLane = Lane.new.weight_(inf).sample_(this.timeChooser.chosenLane.sample);
			{ this.chosenLanes.add(newLane)}, {^ "No playableTimeLane available".postln})
	}


// =========== PLAYING  AND SCHEDULING =====================

playChosen{
		^ this.playChosenLanes}  // needed for neat nesting


playChosenLanes{  // doesn't choose fresh Lanes -  sticks with last choices
		                        this.integrityCheck; // dont think has opportunity to do anything
		          		       this.hasActiveTimeChooser.if (
		                         	{   /*this.journal.add( \hasActiveTimeChooser -> this.timeChooser);
				                            this.journal.add( \activeTimeLane -> this.chosenTimeLane);
			                        	*/
				                           this.chosenLanes.do {|eachLane |
				                            eachLane.playWithChosenTimeLaneForParent(this.chosenTimeLane, this)}},
				                             {this.chosenLanes.do  { |eachLane | eachLane.play }     }  )
	                                }


play {                 //  PRINCIPLE: multiple hits of plain play always produce new choices
		                  // journals previous choices,
			this.chooseLanes;                         // empties out previous choices
		  //   this.journal.add( \chosenLanes -> this.chosenLanes.asArray);
		  //   this.outBus.debug("In Chooser"); // stereo
		    this.name.debug("In Chooser"); // stereo

		    this.outBus.isNil.not.if {
			        this.chosenLanes.debug("Hit chosen Lanes");
			        this.chosenLanes.do
			         {arg eachLane; eachLane.outBus_(this.outBus)}};
		              // stereo   // may be interesting if we go nested
		    this.playChosenLanes
	}



pause{
		this.chosenLanes.do  { |eachLane | eachLane.pause} }

resume{
		this.chosenLanes.do  { |eachLane | eachLane.resume} }

stop { this.free}
kill {this.stop  } //to give uniform nesting protovol in wrapper for nester Loopable S's }

	deepKill {this.chosenLanes.do	{ arg each; each.deepKill};
		("Chooser " + this.name + " just passed on deepkill to chosen lanes ").postln
	}


	free {
		      //this.chosenSynthsHaveBeenFreed.not.if { this.allChosenSynths.free};

		  this.chosenSynthsHaveBeenFreed.not.if {
			this.allChosenSamples.do { arg each; each.free } };
		   this.chosenSynthsHaveBeenFreed_(true);
	} // Fixed clap2 !!!!


//========= DURATION =========

// smart durations not needed for lane to DO right thing but needed
// in case this chooser is nested and it needs  to REPORT right thing
// to enclosing seqiences or choosers

calculateSmartDurationWithChosenTimeLane {
		// this.debug("alculateSmartDurationWithChosenTimeLane");
	   this.chosenLanes.do {|eachLane |
			// eachLane.debug(" about to calculateSmartDurationWithChosenTimeLane");
	         eachLane.calculateSmartDurationWithChosenTimeLaneForParent(this.chosenTimeLane)}}

calculateSmartDurationWithNoActiveTimeLane{
	             this.chosenLanes.do {|eachLane |
	                     eachLane.calculateSmartDurationWithNoActiveTimeLane}}

duration{                                   // sequencer calls this to find out when to sequence
		this.lanesNotChosenYet.if
		    {this.debug("No choices made yet  when querying Chooser duration- should not happen"); ^0};
	^	this.maxLaneDuration  // looks dubious - what if cut by timelane? - nah - looks OK
	}

durations{
		this.lanes.do{ |eachLane| eachLane.sample.name.postln;
			eachLane.sample.duration.postln;}}


durationOfChosenTimeLane{                     //just  to lower coupling in lane
		^this.chosenTimeLane.duration
	}


maxLaneDuration{
		var durations;
		var max;
		durations = chosenLanes.collect{ arg eachLane, i;  //eachLane.debug("max laneduration");
			//eachLane.sample.name.debug("lane name in MAX");
			//eachLane.smartDuration.debug(" lane smartduration in MAX");
			//eachLane.debug(" eachlane debug");
			//eachLane.smartDuration.debug("eachLane smartDuration" );
			eachLane.smartDuration};
			//durations.debug("SMART DURATIONS OF EACH LANE GIVEN TO FIND MAX")
		max = durations.maxItem{arg item, i; item};
		// max.debug("max duration in chooser when checking duration");
		//(max==inf).if {this.halt};
		^max
	}




// PROTOCOL NEEDS CHECKING FOR UNNEEDED FLUFF & CRUFT

// used where different lanes have samples using different tempi,
// so that smart durations in beats translate differently into seconds
// as in clip wwrappers and nysteds. For obvious reasons, Lanes &
//(why chooser wrappers?) also need to undersand

xSmartDuration {
		var  smartDurations;
		var  inSeconds;
		var  max;
		//this.debug("in xhooser");
	    smartDurations = this.chosenLanes.collect
		        { arg eachLane ; eachLane.xSmartDuration.postln};
        inSeconds =  smartDurations.collect
		        { arg eachSmartDuration ; eachSmartDuration.asSeconds };
	    max =  inSeconds.maxItem({ arg item, i; item });
		// max.debug("seconds");
		^max
}

}



	