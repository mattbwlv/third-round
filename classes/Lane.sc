// SHOULD  HAVE COMMON SUPERCLASS WITH TIMELANE
//Lanes in time choosers CAN contain samples- but mainly they contain durations U
//UPDATED


Lane {
	var <>weight;
	var <>  tempo; // lanes can now have a tempo = allows play command to control tempo for midi clips
	var <> loop;
	var <> loopTimes;   // usually inf
	var <> stop;
	var  < outBus;
	var  <> parentName;
	var  <  sample;                // Sample has protocol -
	                                       //play, duration, name, repeatTimes, synth
	                                       // NESTING pretty sure could allow SAMPLE to be
	                                       // chooser and so have nesting
                                           //  Lane could  directly hold Xhooser or Time chooser  -
	                                       //  given that Lane needs to know TC duration for soft & hard stop
	                                       // but that  would be icky and make cleaning memory hard
	                                       // so we pass time chooser to sample as a parameter when needed


cleanUp {
		this.cleanUpSample;
		this.cleanUpRest
	}

cleanUpSample	 {
		this.sample.cleanUp
	}


cleanUpRest {
		this.weight_ (nil); // never assigned - just for sommon protocol
		this.tempo_(nil); // never assigned - just for sommon protocol
		this.loop_(nil);
		this.loopTimes_(nil);
		this.stop_(nil);
		// this.outBus_(nil);
		this.parentName_(nil);
	}


group{ arg g;
		this.sample.group_(g)
	}

sample_{
		arg aSample;
		sample = aSample.kopy;
		// not needed for new instacne but may help with group & parent tracking
		sample.parentName_(this.parentName);
		^this
	}

	outBus_{
		arg aBool;
		outBus = aBool;
		// outBus.debug("Setting outBus in Lane");
		this.sample.outBus_(outBus);
		^this
	}


	deepKill {this.sample.deepKill ; "'lane just passed on deepKill to sample".postln}

	bugFix { arg num ; this.sample.bugFix (num) }

*new { ^ super.new.init}

*null { ^super.new}
	                                       // Null Lane helps  whenno lane can be  validly  chosen
	                                        // More convenient than  empty list
	                                       // the only tricky parts of the protocol that a null lane needs to respond to
	                                       // intelligenty are isNull, play and duration related  - but
	                                       // mostly these are delegated to DummySample
	                                       // But null lane should be initialised to constain instance  of dummySample
	                                        // instead of goDummy Hack - tidy this up.
	                                       // aha - but DummySample also handles case when Lane OK but sample is nil



init{
	weight = 1;
	loop = false;
    stop = \hard ;       /// eh????????? see in this class hashardstop & hassoftstop
	loopTimes = inf
	}

	choose{
		        this.sample.isNil.if { "SampleBank not loaded".postln;   ^nil};
		        this.sample.choose /* just in case its a chooser or sequence*/
	}

clean {
		////this.copy
	}

//  COPY
//  COPY
//  COPY
//  NB STANDARD  COPY WILL WORK BECASUE:
	      // want to make sure ref to node id (synth) is not shared)
		//  doesnt matter if buffer shared
	  ///  instacne of synth is not created till sample is played
	 //  WHICH IS AFTER  the copy has been made by loop[able sequecne
	// WRONG!!!!!!! these will be shared samples by the two lane copies  !!!!!!!!!!!


kopy{ var me;
		  /// used in loopablesequecne
		// we just want fresh lanes to get us fresh sample insatcnes
		// to avoid sinadvertent ample instacne reuse
		// and wrong node getting killed
		// got to be kopy or will be infinite loop
		// make a copy of me
		//copy my lanes separaelt using bog standard copy
		// so its kinds semi deep copy
		// copies will share the samples
		// butsamp[les havent been played yet.
		// so have no synths stsoted in synth instacne variable
		//each play will create a new node ID and a  create & stire new synth
		//the two samples will save synths spereately
		// and so handle swithcing on and off propely (play & free)
		me = this.copy;
		me.sample_(this.sample.kopy);
		// but what if sample is a wrapper???? SO why does KOPY screw up?
		^ me }




	/*
copy {
	var me;
	me = Lane.new;
		me.weight(this.weight);
		me.tempo(this.tempo);
		me.loop(this.loop);
		me.loopTimes(this.loopTimes);
		me.stop(this.stop); // copy
		me.outbus_(this.outbus);
		me.sample_(this.sample.copy);
		^ me
	}
*/


printOn { | aStream |

		this.isNull.if { aStream << "a " << "null"  << this.class.name; ^aStream};
		aStream << " ** " /*<< this.class.name */<< " w " << this.weight.asString ;
		aStream << " "<<  this.stop.asString << " " ;
		this.hasLoop.if( {   aStream <<  " loops "  });
		this.sample.isNil.if{this.sample.debug("Warning -  sample not found in sample bank");  ^ nil};
		this.sample.isString.if{  this.sample.debug("Warning -   sample not loaded");  ^ nil};
		this.sample.isSymbol.if( {   aStream <<  " " << this.sample.asString; ^aStream });
		this.sample.isNil.not.if( {   aStream <<  "sample named — " <<           this.sample.name.asString << " — basic duration ";
			                                     aStream << this.sample.basicDuration << " ";
			                                     aStream <<  " smart status — " << this.smartDuration << " "

		});
		^aStream}


//===== INIT in Dummy Sample case ===================

dummyInit{
sample = DummySample.new }   // Unused - delete


goDummy{ this.sample_(DummySample.new); ^ DummySample.new} //Tidy up - nulls should be inited with a dummy
	                                                                                                      // However also used when sample is nil
//=============   TESTING Queries & Acessors   ====================

hasNestedChooser{    // could be a looped sequence - need to allow for
		^ this.sample.isChooser}


	isNull{
		^ (this.weight ==nil) }

hasInfiniteWeight {
		^ (this.weight ==inf) }

hasZeroWeight{
		^ (this.weight ==0) }

hasHardStop {
		^this.stop == \hard}

hasSoftStop {
		^this.stop == \soft}

hasLoop {
		^this.loop == true}

hasNoLoop {
		^this.loop == false}


hasNoStop {}    // do we need  this? NO!

	synth{^ this.sample.synth }

	// for double nesting should return a list of synths all up the call chain. but just try it for now.   // may need to use stop or clear instead of free(kill) in loopable sequencers


hasActiveTimeChooser{
		arg aParentChooser ;
		var good;
		aParentChooser.isNil.if { ^ false};
				good = aParentChooser.hasActiveTimeChooser;
		        "time chooser hasActiveTimeChooser". postln;
		^ good
	}



//=============   INDIRECT SETTERS  ====================

reps{ arg aNum;
		this.sample.reps_(aNum)
	}

vol{ arg aNum;
		this.sample.volume_(aNum)
	}

	//==========================================
	// NESTING
	//==========================================

	nest{arg aChooser; // works just as well for loop seqs
		var wrapper;
		wrapper = XhooserWrapper.new;
		wrapper.name_("containing" + aChooser.name);
		wrapper.wrap(aChooser);
		this.sample_(wrapper) }


//=============   setters  ====================

hardStopOn	{
		this.stop_ (\hard)}

softStopOn{
		this.stop_ (\soft)}

neitherStopOn{}  //do we need  this?

loopOn {
		this.loop_ (true)}

loopOff {
		this.loop_ (false)}

namedSample{
		arg aSymbol;
		this.sample_(SampleBank.sampleDef(aSymbol));
		// this.sample.debug("sample loaded from SampleBank by namedSample");
	}

trim{
		arg aTrimTool;
		this.sample.withTrim(aTrimTool);
		this.debug("Updating synthdef for trim");
		this.sample.createSynthDef;
	}

namedClip{
		arg aSymbol;
		this.sample_(ClipBank.clipDef(aSymbol));
	}



//============ ACTIONS ================

	play{ // if chooser called this (& only chooser can), then we have no active chooser
		// this.hasLoop.debug("value of has loop in play in Lane");
		this.hasLoop.if( {this.sample.loopOn}, {this.sample.loopOff});
        this.outBus.isNil.not.if { this.sample.outBus_(this.outBus)}; // stereo
		//this.outBus.debug("In Lane"); // stereo

		this.sample.noActiveTimeChooser_(true) ;
		//this.sample.noActiveTimeChooser.debug("this.sample.noActiveTimeChooser in lane");
		this.tempo.isNil.if { ^  this.sample.play };
		this.sample.play(TempoClock.new (this.tempo) )

		//this.isNull.if {^nil};
		//this.sample.isNil.if({ ^nil}, {this.sample.play})
		// needs to deal with case if lane is empty
        	}

pause {
		this.sample.pause
	}

resume {
		this.sample.resume
	}


playWithChosenTimeLaneForParent{   //KEY METHOD - NB  TAKES A PARAMETER
		arg chosenTimeLane, aParent;      // can only be called by chooser
		var timeLaneDuration;
		//this.debug("playWithChosenTimeLaneForParent in LANE");
			this.sample.noActiveTimeChooser_(false) ;
		timeLaneDuration = chosenTimeLane.duration;
		this.isNull.if {^nil};
		this.sample.isNil.if { debug("Sample not loaded") };
		this.sample.isString.if { this.sample.debug("Sample not loaded") ; ^nil};
		this.sample.isSymbol.if { this.sample.debug("Sample not loaded") ; ^nil};
		this.sample.isNil.if({ ^nil},
			{   this.hasLoop.if( {this.sample.loopOn}, {this.sample.loopOff});
				this.hasHardStop.if {this.sample.hardPlay(timeLaneDuration)};
				this.hasSoftStop.if {this.sample.softPlay(timeLaneDuration)};
		})                                                   // needs to deal with case if lane is empty
        }


// ============ DURATION - some may be redundant or need refactoring ================


//================ DEFINITIVE DURATION FOR ALL EXTERNAL QUERIES =============

duration{
		this.isNull.if {^0};
		^this.smartDuration
        	}

//================  DURATION IF NO TIME LANE - used in internal calculations only ===================

localDuration{
		(this.sample.class == DummySample).if { ^0 };
		this.sample.isNil.if { this.goDummy };  //Tidy up- nulls should just  be inited with a dummy
		//this.sample.debug("local sample in lane");
		// this.sample.chooser.debug("chooser in wrapper in lane");
		// this.sample.duration.debug("local duration of sample in lane");
		^ (this.sample.duration) *  (this.loopDurationMultiplier) }



// ========== SMART DURATIONS==================
// Smart Durations are not needed to hard & soft stop correctly correctly,
// but are needed to sequence choosers correctly so that total durationof soft stops is known in advance
// They are calculated here but stored in the sample

sampleOK{
		this.sample.isNil.if{  debug("sample not loaded in Lane quality check"); ^false};
		this.sample.isSymbol.if{ debug("sample loaded only as symbol in Lane quality check - load SampleBank"); ^false};
		this.sample.isString.if { debug("sample not in SamplBank"); ^false};
	^ true}

sampleNotOK { ^ this.sampleOK.not}


smartDuration{
		this.sampleNotOK.if {^0};
		^this.sample.smartDuration}


// =======  Chooser decides which of these two methods  for calculating smart duration to call ==========
// This is not needed to hard & soft stop correctly correctly - just for sequencing

calculateSmartDurationWithNoActiveTimeLane{
		this.sample.isNil.if { this.goDummy}; //Tidy up- nulls should just be inited with a dummy
		this.sample.isNil.if { debug("Sample not loaded") };
		this.sample.isString.if { this.sample.debug("Sample not loaded") };
		this.sample.isSymbol.if { this.sample.debug("Sample not loaded") };
        this.sample.choose;  //needed for  nested case

		this.sample.smartDuration_(this.localDuration);
		//this.localDuration.debug("local duration - no active time chooser");
        ^this.localDuration }


calculateSmartDurationWithChosenTimeLaneForParent{  //KEY METHOD
		                                                                             //- NB  TAKES A PARAMETER
	arg chosenTimeLane, aParent;
		var timeLaneDuration;
		//this.debug("who is setting to inf");
		chosenTimeLane.isNil.if{

			this.sample.isNil.if { debug("Sample not loaded"); ^nil};
			this.sample.isString.if { this.sample.debug("Sample not loaded"); ^nil};
			this.sample.isSymbol.if { this.sample.debug("Sample not loaded"); ^nil};
           // best we can do
			this.sample.smartDuration_(this.sample.basicDuration)};


		timeLaneDuration = chosenTimeLane.duration;
        // timeLaneDuration.debug("who is setting to inf");
		this.isNull.if {^nil};
		this.sample.isNil.if { debug("Sample not loaded"); ^nil};
		this.sample.isString.if { this.sample.debug("Sample not loaded"); ^nil};
		this.sample.isSymbol.if { this.sample.debug("Sample not loaded"); ^nil};
		this.sample.choose;
		//this.debug("going for kill in lane");
			this.hasHardStop.if {
			//timeLaneDuration.debug("hard stop branch");
			^ this.sample.hardDuration(timeLaneDuration)};  // misleading name - this is a setter for sample

				this.hasSoftStop.if {
			 // timeLaneDuration.debug("in Lane soft stop branch");
			^ this.sample.softDuration(timeLaneDuration)};  // misleading name - this is a setter for sample
		//this.debug.("neither hard not soft  in lane - should not happen");

	}

kill {
		this.sample.kill;
		this.name.debug(" PASSING ON INSTRUCTED FREE Thru lane " );
	}


// ========== duration helper in case of finite repetition loops==================
loopDurationMultiplier{
		this.hasNoLoop.if{^1};
		this.hasLoop.if{^this.loopTimes};
	}


durationGivenNRepeats{   // fortunately not called
		arg num ;
		^ num* (this.sample.duration);  // dont think that sright
			}

xSmartDuration	{
		^ this.sample.xSmartDuration}




//============= Appear to be  UNUSED  ===============

noOfRepeatsRequiredForSoftStopWithTimeLaneOfDuration{
		arg num ;
		this.sample.isNil.if { "No sample found when calulating soft stop".postln; this.halt};
		(this.sample.duration == inf).if{^0};
		this.isNull.if{"Should not happen".postln};
		^ num/ (this.sample.duration)	}



durationForSoftStopGivenTimeLaneWithDuration{
		arg num ;
		var repeats;
				repeats = this. noOfRepeatsRequiredForSoftStopWithTimeLaneOfDuration (num);
			^	this.durationGivenNRepeats(repeats);
           	}



durationOfChosenTimeLane{
		arg aParentChooser ;
		 aParentChooser.isNil.if { "No parent chooser, so can't check chosen  timeLane duration".postln;  ^nil};
				^aParentChooser.durationOfChosenTimeLane;
	}


performanceDuration{
	arg aParentChooser ;
	var guideDuration;
	var activeTC ;
	   "in performance  duration".postln;
		activeTC = this.hasActiveTimeChooser(aParentChooser);
		activeTC.not.if  {"no active time chooser".postln;  ^ this.localDuration};
		guideDuration= this.durationOfChosenTimeLane;
		(this.hasHardStop.and({activeTC})).if {"hard stop".postln; ^ guideDuration};
		(this.hasSoftStop.and({activeTC})).if { "soft stop".postln;
				^ this.durationForSoftStopGivenTimeLaneWithDuration(guideDuration)};
			}





 }