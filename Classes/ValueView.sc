// ValueView: a wrapper to make custom widgets
// that hold a mapped 'value' and normalized 'input',
// and draws custom layers in a UserView, with mouse/arrow key interaction

ValueView : View {
	var <spec, <value, <input, <action;
	var <>wrap = false;
	var <>limitRefresh = false, <maxRefreshRate=25, updateWait, allowUpdate=true, updateHeld=false;
	var <>suppressRepeatedAction = true;
	var <>autoRefresh = true;         // refresh automatically when layer properties are updated
	var <>broadcastNewOnly = false;   // only notify dependents on input/value update if new value, otherwise anytime value is set

	// interaction
	var mouseDownPnt, mouseUpPnt, mouseMovePnt;
	var <>mouseDownAction, <>mouseUpAction, <>mouseMoveAction;
	var <>valuePerPixel;
	var <>scroll = true;

	/*
	 * step vars describe the step amount
	 * as a percentage of the range of the spec
	 * e.g. keyStep of 0.05 means 20 key strokes
	 * to cover the full range of the value
   */
	var <>keyStep = 0.03333333;    // scale step when arrow keys are pressed
	var <>scrollStep = 0.01;       // scale _input_ step when scroll wheel moves

	var <>xScrollDir = 1;          // change scroll direction, -1 or 1
	var <>yScrollDir = -1;         // change scroll direction, -1 or 1, -1 is "natural" scrolling on Mac
	var <>keyDirLR = 1;            // change step direction of Left/Right arrow keys (1=right increments)
	var <>keyDirUD = 1;            // change step direction of Up/Down arrow keys (1=up increments)

	var <userView;
	var <layers;                   // array of drawing layers which respond to .properties

	*new { |parent, bounds, spec, initVal |
		^super.new(parent, bounds).superInit(spec, initVal);
	}

	superInit { |argSpec, initVal|
		spec = argSpec ?? \unipolar.asSpec.copy;
		value = initVal ?? spec.default;
		input = spec.unmap(value);
		action = {};
		valuePerPixel = spec.range / 200; // for interaction: movement range in pixels to cover full spec range
		updateWait = maxRefreshRate.reciprocal;

		userView = UserView(this, this.bounds.origin_(0@0)).resize_(5);
		userView.drawFunc_(this.drawFunc);

		// over/write mouse actions
		// TODO: make a complete list

		userView.mouseMoveAction_({
			|v, x, y, modifiers|
			mouseMovePnt = x@y;
			mouseMoveAction.(v,x,y,modifiers)
		});

		userView.mouseDownAction_({
			|v, x, y, modifiers, buttonNumber, clickCount|
			mouseDownPnt = x@y;
			mouseDownAction.(v, x, y, modifiers, buttonNumber, clickCount)
		});

		userView.mouseUpAction_({
			|v, x, y, modifiers, buttonNumber|
			mouseUpPnt = x@y;
			mouseUpAction.(v, x, y, modifiers, buttonNumber)
		});

		// NOTE: if overwriting the following UserView actions,
		// include a call to the step functions within to retain
		// key inc/decrement capability
		userView.mouseWheelAction_({
			|v, x, y, modifiers, xDelta, yDelta|
			if (scroll) {
				this.stepByScroll(v, x, y, modifiers, xDelta, yDelta);
			};
		});

		userView.keyDownAction_ ({
			|view, char, modifiers, unicode, keycode, key|
			this.stepByArrowKey(key);
		});

		this.onResize_({ userView.bounds_(this.bounds.origin_(0@0)) });
		this.onClose_({ }); // set default onClose to removeDependants
	}

	stepByScroll { |v, x, y, modifiers, xDelta, yDelta|
		var dx, dy, delta;
		dx = xDelta * xScrollDir;
		dy = yDelta * yScrollDir;
		delta = scrollStep * (dx+dy).sign;
		this.inputAction = input + delta;
	}

	stepByArrowKey { |key|
		var dir, delta;
		dir = switch( key,
			16777234, {-1 * keyDirLR},  // left
			16777236, { 1 * keyDirLR},  // right
			16777237, {-1 * keyDirUD},  // down
			16777235, { 1 * keyDirUD},  // up
			{^this}  // break
		);

		// delta = max(step, 1e-10) * dir * arrowKeyStepMul;
		// this.valueAction = value + delta;
		delta = keyStep * dir;
		this.inputAction = input + delta;
	}

	// overwrite default View method to retain freeing dependants
	onClose_ { |func|
		var newFunc = { |...args|
			layers.do(_.removeDependant(this));
			func.(*args)
		};
		// from View:onClose_
		this.manageFunctionConnection( onClose, newFunc, 'destroyed()', false );
		onClose = newFunc;
	}

	update { |changer, what ...args|
		// refresh when layer properties change
		if (what == \layerProperty) {
			autoRefresh.if{this.refresh};
		}
	}

	drawFunc { this.subclassResponsibility(thisMethod) }

	value_ { |val|
		var oldValue = value;
		// TODO: should wrap option be default behavior?
		// or should subclass add this via method override
		// as desired?
		value = if (wrap) {
			val.wrap(spec.minval, spec.maxval);
		} {
			spec.constrain(val);
		};
		input = spec.unmap(value);
		this.broadcastState(value!=oldValue);
	}

	// set the value by unmapping a normalized value 0>1
	input_ {|normValue|
		var oldValue = value;
		input = if (wrap) {
			normValue.wrap(0,1);
		} {
			normValue.clip(0,1);
		};
		value = spec.map(input);
		input = spec.unmap(value); // remap input back so it's stepped with the value TODO: reconsider?
		this.broadcastState(value!=oldValue);
	}

	valueAction_ {|val|
		var oldValue = value;
		this.value_(val);
		this.doAction(oldValue!=value);
	}

	inputAction_ {|normValue|
		var oldValue = value;
		this.input_(normValue);
		this.doAction(oldValue!=value);
	}

	broadcastState { |newValue=true|
		// update the value and input in layers' properties list
		// note: because this sets p values directly, it doesn't trigger an update
		layers.do({|l| l.p.val = value; l.p.input = input});

		// notify dependants
		if (newValue) {
				this.changed(\value, value);
				this.changed(\input, input);
				this.refresh; // TODO: consider making this a global flag, e.g. refreshNewOnly
		} {
			if (broadcastNewOnly.not) {
				this.changed(\value, value);
				this.changed(\input, input);
			}
		}
	}

	action_ { |actionFunc|
		action = actionFunc;
	}

	doAction { |newValue=true|
		if (suppressRepeatedAction.not or: newValue) {
			action.(this, value, input)
		};
	}

	spec_ {|controlSpec, updateValue=true|
		var rangeInPx;
		if (controlSpec.isKindOf(ControlSpec).not) {
			"Spec provided isn't a ControlSpec. Spec isn't updated.".warn;
			^this
		};
		rangeInPx = spec.range/valuePerPixel; // get old pixels per range
		spec = controlSpec;
		this.rangeInPixels_(rangeInPx); // restore mouse scaling so it feels the same
		updateValue.if{
			this.value_(value);         // also updates input
			this.broadcastState(true);  // force a state broadcast because input will be updated, but value will stay the same
		};
	}

	refresh {
		if (limitRefresh) {
			if (allowUpdate) {
				userView.refresh;
				allowUpdate = false;
				AppClock.sched(updateWait, {

					if (updateHeld) { // perform deferred refresh
						userView.refresh;
						updateHeld = false;
					};
					allowUpdate = true;
				});
			} {
				updateHeld = true;
			};
		} {
			userView.refresh;
		}
	}

	rangeInPixels_ { |px|
		valuePerPixel = spec.range / px;
	}

	rangeInPixels { ^spec.range / valuePerPixel }

	maxRefreshRate_ { |hz|
		maxRefreshRate = hz;
		updateWait = maxRefreshRate.reciprocal;
	}
}


// TODO
// reconsider: input_ remap input back to it's stepped with the value
// ^^ this has muddled what the input resolution can be, made step=0 a problem,
// and somehow circular dragging broke (see radial dial example)
// when stroking the outside of the range, fix the line termination

// Knob behavior to port:
// - k.enabled = false; // disables interaction, dims the knob
// add shift_scale, ctl_scale, alt_scale
// add modelUpdateOnly flag, which if true interacting with the view doesn't
//     update the display (does action with value without updating ui), expecting the
//     model to update it
// have an inputStep and valueStep, which are exclusive of one another
//     so if the spec is non-linear, the step can apply to the input rather than the output

// double check the arrow key IDs - compare to Knob

// add a visual cue when widget is focussed

// NOTE: as the value of spec.step increases, scrolling become less practical,
// and in fact if the scrollStep is any significant value below the step (as a normalized value)
// it won't work (spec.step will always constrain the scrolling amount back to it's current position)


// TODO: annular wedge and arc stroke shows buggy artifact at
// start point when position isn't at a 90 degree position
// and annular wedge start/endpoints don't join for some reason (see Pen.joinStyle = 0,1,2)
// although the example for Pen.joinStyle looks OK

// consider adding easy way to make bezier curves instead of squared off stuff
// e.g. on the arrow, consider *path, *curveTo, or *quadCurveTo
// https://www.toptal.com/c-plus-plus/rounded-corners-bezier-curves-qpainter
