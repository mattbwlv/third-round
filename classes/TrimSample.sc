TrimSample : Sample {

var <> trimStartGridPoint = 0;
var <> trimEndGridPoint = 16;
var <> anchorGridPoint = 0;     // number between 0 and  grid- playBuf Trigger phase
var <> grid = 16;                       // eg 12 &4  quarter notes  VARIABLE
var  <> printOutCount = 0;      // not really working - different instance?

// synonyms
trimStart { ^ this. trimStartGridPoint}
trimEnd { ^ this. trimEndGridPoint}
anchorPoint { ^ this. anchorGridPoint}
anchorFraction { ^ this. anchorGridFraction}
tempo {^ SampleBank.tempo}

   // Getting values from TRIMTOOL
withTrim {arg aTrimTool;
		this.grid_(aTrimTool.grid);
		this.trimStartGridPoint_ ( aTrimTool.start)  ;
		this.trimEndGridPoint_( aTrimTool.end)    ;
		this.anchorGridPoint_   (aTrimTool.anchor)  ;
        this.debugDump
}

//derived values
trimWidthInGridPoints {  ^ this.trimEnd - this.trimStart}              //OK
trimWidthAsFraction {^ this.trimWidthInGridPoints / this.grid}		// true
sampleDurationInSecs { ^ this.sampleDurationInBeats * this.tempo}
sampleDurationInBeats {  ^ this.buffer.duration*this.tempo}                              // OK
trimDurationInBeats {  ^ this.trimWidthAsFraction* this.sampleDurationInBeats}
trimDurationInSecs {^ this.trimDurationInBeats * this.tempo}
anchorGridFraction { ^ this.anchorPoint / this.grid}

// fraction between 0 and 1 -  implemented as playBuf Trigger phase

//conversion
gridToBeats{arg gridPoint;
		^ this.buffer.duration* this.tempo *(gridPoint/this.grid)}
gridToFrameNum{arg gridPoint;
		^ this.buffer.numFrames* (gridPoint/this.grid)}


// UGen stuff

frequency { ^Impulse.kr(this.trimWidthInGridPoints*this.buffer.sampleRate)}

	/*
playHead {^ Phasor.ar( this.freq, 1,
			this.gridToFrameNum(this.trimStart),
			this.gridToFrameNum(this.trimEnd),
			this.gridToFrameNum(this.anchorPoint))}
	*/

// THIS GETS COMPILED AND SENT TO SERVER WHEN WARMING UP!!!!!!!!!
		                  // CANNT WORK THE WAY I CEUUENTLY CREATE SYNTH DEFS - ANd THEY Take TIME
		                  // AHA! trim reruns creatSYnthDEf!!
createSynthDef {
		    //this.debugDump ;
			SynthDef(this.name ,
			{ |loop=0, volume=0.5, outputBus=0 freq = 0 playHead =0|
			freq = this.frequency;
			playHead =Phasor.ar( freq, 1,
			this.gridToFrameNum(this.trimStart),
			this.gridToFrameNum(this.trimEnd),
			this.gridToFrameNum(this.anchorPoint));
			Out.ar(outputBus,
				     BufRd.ar(    this.buffer.numChannels,
					                    this.buffer.bufnum,
					                     playHead,
					                     0,
					                     2)              // interpolation - which would sound best?
					*volume )
			}).add;
		}


// ============	====================

// DOES NOT  WORK - server side magic with set

setSampleLoopTo{arg aNum;
		(aNum<1).if ({ // just been set to no loop - but set asynchronously
                             // set impulse to zero?
			synth.set(\freq, -1 ,\playHead, -1)})}



softPlay{ arg tcDuration; // sent as beats =  so convert to beats....
	     	 var  softPlayKillClock =   TempoClock(SampleBank.tempo);

		     (tcDuration <= this.basicDuration).if{this.loopOff;  this.setSampleLoopTo(0) };
              this.play;
			  softPlayKillClock.sched( this.softDuration(tcDuration),
				{ // this.setSampleLoopTo(0); // does nothing, sadly
				  this.synth.free; // frees the stored instance
			      this.name.debug(" SOFT STOP client-side — node ID" + synth.asNodeID.asString);
				   tcDuration.debug(                           "    tcDuration");
				   this.sampleDurationInBeats.debug( "    sample duration");
				  this.clockSoftDuration(tcDuration).debug("    soft stop duration " );
				   softPlayKillClock.stop;})
		}


clockSoftDuration {arg duration;
		^  this.sampleDurationInBeats * this.neededRepeatsFor(duration)}

neededRepeatsFor{  arg softStopDuration;
		 var containedRepeats ;
		  var modulo;
		  containedRepeats=  (softStopDuration/ this.basicDuration).floor;
	      modulo = softStopDuration %  this.basicDuration;
		 (modulo> 0 ).if( { ^ containedRepeats +1}, { ^ containedRepeats })
	}



// ============ INTRINSIC DURATION

basicDuration{
		^this.trimDurationInBeats }  //Clearer name to communicate this is duration of sample
	                             // when not changedby external factors

duration{
		^this.basicDuration
	}



debugDump	 {
this.sampleDurationInBeats.debug("sampleDurationInBeats");
		this.frequency.debug("frequency");
this.trimStart.debug("trim start");
this.trimEnd.debug("trim end");
this.anchorPoint.debug("anchor point");
//this. playHead.debug("playhead");
this. buffer.debug("buffer");
this. frequency.debug("frequency");
this. buffer.numFrames.debug("buffer.numFrames");
//this. playBufStartPos.debug("playBufStartPos");
// this. triggerPhase.debug("triggerPhase");
this. trimDurationInBeats.debug("trimDuration in beats");
this. trimDurationInSecs.debug("trimDuration in secs");
this.debug ("in Trimsample");

	}

//phase appears  useless for anchor — does something but not anchor
// maybe have a stepped thing so initial start pos is diff from start

/* adapt this to fix anchor!

	{Select.kr(
	    Stepper.kr( Impulse.kr(4), 0,0,10,1,0	),
       	                            [10,11,12,13,14,15,16,17,18,19,20]
	)}.plot(1,minval:0,maxval:21);
	*/





}

