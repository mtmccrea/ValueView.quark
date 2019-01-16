/*
 * This is a superclass of drawing "layers" used by ValueView and its subclasses.
 * access properties with instance variable 'p'
 * access ValueView variables with instance variable 'view'
*/

ValueViewLayer {
	var view, <>p; // properties

	*new { |valueView, initProperties|
		^super.newCopyArgs(valueView).init(initProperties)
	}

	*properties { this.subclassResponsibility(thisMethod) }

	init { |initProperties|
		p = initProperties ?? { this.class.properties };
		this.register;
	}

	register {
		this.addDependant(view);
	}

	// catch setters and forward to setting properties
	doesNotUnderstand { |selector, value|
		var asGetter = selector.asGetter;
		var curVal = p[asGetter];

		if (selector.isSetter) {
			if (curVal.notNil) {
				p[asGetter] = value;
				^this.changed(\layerProperty, asGetter, value);
			}
		} {
			if (curVal.notNil) {
				^p[selector]
			}
		};

		// unknown selector, warn and return self
		"'%'' property not found".format(asGetter).warn;
	}

	properties { ^p }

	getJoinIndex { |style|
		^if (style.isKindOf(Number)) {
			style
		} {
			switch(style,
				\miter, { 0 },    // pointed corners, will overshoot vertex at sharp angles
				\round, { 1 },    // round corners
				\bevel, { 2 },    // snubbed corners
				{ 0 }             // default
			)
		}
	}

	getCapIndex { |style|
		^if (style.isKindOf(Number)) {
			style
		} {
			switch(style,
				\butt,   { 0 },    // square end that doesn't overshoot endpoint, i.e. "butts" up angainst the endpoint
				\flat,   { 0 },    // same as butt, as Qt calls it
				\round,  { 1 },    // round end that overshoots endpoint by half pen width, i.e. center of the brush stops on the endpoint
				\square, { 2 },    // square end that overshoots endpoint by half pen width, i.e. center of the brush stops on the endpoint
				{ 0 }              // default
			)
		}
	}

	listProperties {
		postf("% (\n", this.class.name);
		this.p.keysValuesDo{ |k,v| postf("\t% : %\n", k, v) };
		")".postln;
	}
}
