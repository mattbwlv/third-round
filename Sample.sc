// UPDATE
Sample{                     // synthDef is creaed by warmup but sorta lives on server
		var <>buffer ;               // filled by init
		var <>synth;                 // filled in by play - can have many instance playing
		var <> name;               // filled by new
	    var <> wavName;         //  filled by new
	    var <>atomicRepeats;   // not yet used
	    var  <> loop   ;
	    var <> outBus = 0;                   // -1 to 1 doents appear to be workig
	    var  <> smartDuration;  // not need to play correctly. Need to report duration
	                                         // correctly when controlled by external environment

// ============= QUERYING AND SETTING =======
hasLoop {
		^this.loop == true}

hasNoLoop {
		^this.loop == false}

loopOn {
		this.loop_ (true)}

loopOff {
		this.loop_ (false)}

loopStatus{
		this.hasLoop.if( {^1}, {^0} )}

isChooser {^false}

isSymbol { ^ false}

// CREATING & INITIALIZING =========

*new { arg  aName, aWavName;
			var me = super.new;
		     me.name_(aName.asSymbol);
		     me.wavName_ (aWavName);
			^ me.init}

init {
		this.createBuffer;
	    "Wait before warming up".postln}

warmUp {
			this.createSynthDef;
	}

createBuffer{  //  NB audio folder assumed to be in extension folder
		 var classFilePath;
		 var directoryPath ;
		 var wavFilePath;
		var wavFilePathString;
		 var fullWavPath;
		classFilePath = PathName.new(this.class.filenameSymbol.asString);
		//classFilePath.postln;
		directoryPath = classFilePath.pathOnly;
		//directoryPath.postln;
		wavFilePathString ="audio/" ++ this.wavName ++".wav";
		//wavFilePathString.postln;
		wavFilePath =  PathName(wavFilePathString);
		//wavFilePath.postln;
		fullWavPath = directoryPath +/+  wavFilePath;
		//fullWavPath.postln;
		this.buffer_(Buffer.read(Server.default, fullWavPath.fullPath.asString));
		// this.buffer.postln;
               }

	createSynthDef {
		//(this.name == \clap11).if{ outBus= 1};  // daft appraoch - just an experiment
		SynthDef(this.name , {arg loop=0, volume=0.5, outputBus=0;
			Out.ar(outputBus,
				PlayBuf.ar(2, this.buffer.bufnum, BufRateScale.kr(this.buffer.bufnum),
			loop:loop, doneAction:2)*volume )
		}).add;
	}

// ========== PLAYING ===================
play {   synth = Synth(this.name);
		   synth.set(\outputBus, this.outBus);
		this.outBus.debug("In sample");
		     synth.set(\loop, this.loopStatus);}
	                                                               //starts playing as soon as created


	//neither of these stops below look adequate for diff sample types
	// oh - maybe wrappers do it.... YES!!

hardPlay{ arg tcDuration;
	     	 var  t =   TempoClock(SampleBank.tempo); // queried by lane & chooser
		     this.hardDuration(tcDuration);  // tough bug to find on new plays
		     this.play;
		     t.sched( tcDuration,   {this.synth.free})
	}


softPlay{ arg tcDuration;
	     	 var  t =   TempoClock(SampleBank.tempo);
		     this.softDuration(tcDuration);  // tough bug to find on new plays
		     (tcDuration <= this.basicDuration).if{this.loopOff;  this.synth.set(\loop, 0) };
              this.play;
		                                                 // queried by lane & chooser
		      (tcDuration > this.basicDuration).if{
			t.sched( tcDuration + (this.duration/100),   {this.synth.set(\loop, 0)})}
	}

pause {
			this .synth.run(false) }
free {
			this .synth.free}

resume {
			this.synth.run(true) }

choose {   ^ this  /* just needed for recursion*/}


// ============ INTRINSIC DURATION

basicDuration{
		^this.duration }  //Clearer name to communicate this is duration of sample
	                             // when not changedby external factors

duration{
		var aDuration;
		// duration of sample when not multiplied by looping or curtailed by hard or soft stop
		//duration in beats.  (Buffer returns duration iin seconds, so we need to convert)
		//this.buffer.debug("buffer");
		//this.buffer.duration.debug("buffer,duration");
		// LIKELY SERVER NOT STARTING _ REBOOT MAC
		{aDuration = this.buffer.duration}.try
		{^ this.debug("Likely 'Server failing to start' error'- reboot machine- not just supercollider")};

		^SampleBank.secsToBeats(aDuration)
	}



//============= SETTING SMART DURATION  =============
// ---- NOT needed to play hard & soft stops right - just for sequencing

hardDuration {arg duration;                                       // misleading name - its a setter
		            this.smartDuration_(duration);
		           ^  this.smartDuration }  // queried by lane & chooser



softDuration {arg duration;                                             // misleading name - its a setter
		              this.smartDuration_(  this.basicDuration *    this.neededRepeatsFor(duration));
		           ^  this.smartDuration }  // queried by lane & chooser


neededRepeatsFor{
		 arg softStopDuration;
		 var containedRepeats ;
		  var modulo;
		  containedRepeats=  (softStopDuration/ this.basicDuration).floor;
	      modulo= softStopDuration %  this.basicDuration;
		 (modulo> 0 ).if( { ^ containedRepeats +1}, { ^ containedRepeats })
	}

}




