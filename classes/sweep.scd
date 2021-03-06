Table of contents ▼
SuperCollider 3.10.0SweepBrowseSearchIndexes ▼
Classes | UGens > Triggers
Sweep : UGen : AbstractFunction : Object
Triggered linear ramp
Source: Trig.sc
Description
Starts a linear raise by rate/sec from zero when trig input crosses from non-positive to positive.
When rate == 1, Sweep may be used to get a continually-updating measurement of the time (in seconds) since the last trigger.
Class Methods
Sweep.ar(trig: 0.0, rate: 1.0)
Sweep.kr(trig: 0.0, rate: 1.0)
Arguments:
trig
triggers when trig input crosses from non-positive to positive.
rate
rate/sec raise rate
Inherited class methods
7 methods from UGen ► show
8 methods from Object ► show
Instance Methods
Inherited instance methods
90 methods from UGen ► show
157 methods from AbstractFunction ► show
262 methods from Object ► show
Examples

// using sweep to modulate sine frequency
(
{ var trig;
    trig = Impulse.kr(MouseX.kr(0.5, 20, 1));
    SinOsc.ar(Sweep.kr(trig, 700) + 500, 0, 0.2)
}.play;
)
​
​
// using sweep to index into a buffer
b = Buffer.read(s, Platform.resourceDir +/+ "sounds/a11wlk01.wav");
​
(
{ var trig;
    trig = Impulse.kr(MouseX.kr(0.5, 10, 1));
    BufRd.ar(1, b, Sweep.ar(trig, BufSampleRate.ir(0)))
}.play;
)
​
// backwards, variable offset
(
{ var trig, pos, rate;
    trig = Impulse.kr(MouseX.kr(0.5, 10, 1));
    rate = BufSampleRate.ir(0);
    pos = Sweep.ar(trig, rate.neg) + (BufFrames.ir(0) * LFNoise0.kr(0.2));
    BufRd.ar(1, b, pos)
}.play;
)
​
// raising rate
(
{ var trig, rate;
    trig = Impulse.kr(MouseX.kr(0.5, 10, 1));
    rate = Sweep.kr(trig, 2) + 0.5;
    BufRd.ar(1, b, Sweep.ar(trig, BufSampleRate.ir(0) * rate))
}.play;
)
​
///////////////////
b.free
Sweep can be used as a resettable Phasor or Line - one that can start, pause, resume and stop. To get a resettable XLine behavior change the linlin to linexp in the SynthDef below.

(
SynthDef(\lineReset, { |out, start= 0, end= 1, dur= 1, t_trig= 1, run= 1|
    var phasor = Sweep.ar(t_trig, run / dur).linlin(0, 1, start, end, \minmax);
    phasor.poll;
    Out.ar(out, SinOsc.ar(phasor, 0, 0.2));
}).add;
)
a = Synth(\lineReset, [\start, 400, \end, 800, \dur, 2])
a.set(\t_trig, 1)
a.set(\run, 0)
a.set(\run, 1)
a.set(\t_trig, 1)
a.free
​
//shorter duration and downwards...
a= Synth(\lineReset, [\start, 1000, \end, 500, \dur, 0.5])
a.set(\t_trig, 1)
a.set(\run, 0)
a.set(\run, 1)
a.set(\t_trig, 1)
a.free
helpfile source: /usr/local/share/SuperCollider/HelpSource/Classes/Sweep.schelp
link::Classes/Sweep::