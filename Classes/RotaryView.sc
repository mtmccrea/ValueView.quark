// TODO: create a focus function for each, i.e. a change in properties when view is active

RotaryView : ValueView {

	var <innerRadiusRatio, <outerRadiusRatio, boarderPx, <boarderPad;
	var stValue, stInput, >clickMode;

	// create variables with getters which you want
	// the drawing layers to access
	var <direction, <orientation, <bipolar, <startAngle, <sweepLength;
	var <prCenterAngle, <centerNorm, <centerValue;
	var <bnds, <cen, <maxRadius, <innerRadius, <outerRadius, <wedgeWidth;  // units: pixels, set in drawFunc
	var <dirFlag;           // cw=1, ccw=-1
	var <prStartAngle;      // start angle used internally, reference 0 to the RIGHT, as used in addAnnularWedge
	var <prSweepLength;     // sweep length used internally, = sweepLength * dirFlag
	var <levelSweepLength;
	var <majTicks, <minTicks, majTickVals, minTickVals;

	// drawing layers. Add getters to get/set individual properties by '.p'
	var <range, <level, <text, <ticks, <handle, <outline;

	*new {
		|parent, bounds, spec, initVal, startAngle=0, sweepLength=2pi, innerRadiusRatio=0, outerRadiusRatio=1|
		^super.new(parent, bounds, spec, initVal).init(startAngle, sweepLength, innerRadiusRatio, outerRadiusRatio);
	}


	init {
		|argStartAngle, argSweepLength, argInnerRadiusRatio, argOuterRadiusRatio|

		// REQUIRED: in subclass init, initialize drawing layers

		// initialize layer classes and save them to vars
		#range, level, text, ticks, handle, outline = [
			RotaryRangeLayer, RotaryLevelLayer, RotaryTextLayer,
			RotaryTickLayer, RotaryHandleLayer, RotaryOutlineLayer
		].collect({
			|class|
			class.new(this, class.properties)
		});

		// convenience variable to access a list of the layers
		layers = [range, level, text, ticks, handle, outline];

		startAngle = argStartAngle;		// reference 0 is UP
		sweepLength = argSweepLength;
		direction = \cw;
		dirFlag = 1;
		orientation = \vertical;
		wrap = false;
		clickMode = \relative;			// or \absolute
		boarderPad = 1;
		boarderPx = boarderPad;

		this.innerRadiusRatio_(argInnerRadiusRatio); // set innerRadiusRatio with setter to check sweepLength condition
		this.outerRadiusRatio_(argOuterRadiusRatio);

		// intialize pixel unit variables
		maxRadius = this.bounds.width/2;
		outerRadius = maxRadius*outerRadiusRatio;
		innerRadius = maxRadius*innerRadiusRatio;
		wedgeWidth = outerRadius-innerRadius;

		bipolar = false;
		centerValue = spec.minval+spec.range.half;
		centerNorm = spec.unmap(centerValue);

		majTicks = [];
		minTicks = [];
		majTickVals = [];
		minTickVals = [];

		this.defineMouseActions;
		this.direction_(direction);  // this initializes prStarAngle and prSweepLength
	}

	drawFunc {
		^{|v|
			// "global" instance vars, accessed by ValueViewLayers
			bnds = v.bounds;
			cen  = bnds.center;
			maxRadius = min(cen.x, cen.y) - boarderPx;
			outerRadius = maxRadius * outerRadiusRatio;
			innerRadius = maxRadius * innerRadiusRatio;
			wedgeWidth = outerRadius - innerRadius;
			levelSweepLength = if (bipolar,{input - centerNorm},{input}) * prSweepLength;
			this.drawInThisOrder;
		}
	}

	drawInThisOrder {
		if (outline.p.show and: outline.p.fill) {outline.fill};
		if (range.p.show and: range.p.fill) {range.fill};
		if (level.p.show and: level.p.fill) {level.fill};
		if (ticks.p.show) {ticks.fill; ticks.stroke};
		if (range.p.show and: range.p.stroke) {range.stroke};
		if (level.p.show and: level.p.stroke) {level.stroke};
		if (handle.p.show) {handle.stroke}; // fill isn't used because they need not be drawn separately
		if (outline.p.show and: outline.p.stroke) {outline.stroke};
		if (text.p.show) {text.fill; text.stroke};
	}

	defineMouseActions {

		// assign action variables: down/move
		mouseDownAction = {
			|v, x, y|
			stValue = value;
			stInput = input;
			if (clickMode=='absolute') {this.respondToAbsoluteClick};
		};

		mouseMoveAction  = {
			|v, x, y|
			switch (orientation,
				\vertical, {this.respondToLinearMove(mouseDownPnt.y-y)},
				\horizontal, {this.respondToLinearMove(x-mouseDownPnt.x)},
				\circular, {this.respondToCircularMove(x@y)}
			);
		};
	}

	respondToLinearMove {|dPx|
		if (dPx != 0) {
			this.valueAction_(stValue + (dPx * valuePerPixel))
		};
		// this.refresh;
	}

	// radial change, relative to center
	respondToCircularMove {|mMovePnt|
		var pos, rad, radRel;
		pos = (mMovePnt - cen);
		rad = atan2(pos.y,pos.x);					// radian position, relative 0 at 3 o'clock
		radRel = rad + 0.5pi * dirFlag; 	// relative 0 at 12 o'clock, clockwise
		radRel = (radRel - (startAngle*dirFlag)).wrap(0, 2pi); // relative to start position
		if (radRel.inRange(0, sweepLength)) {
			this.inputAction_(radRel/sweepLength); // triggers refresh
			stValue = value;
			stInput = input;
		};
	}

	respondToAbsoluteClick {
		var pos, rad, radRel;
		pos = (mouseDownPnt - cen);
		rad = atan2(pos.y,pos.x);					// radian position, relative 0 at 3 o'clock
		radRel = rad + 0.5pi * dirFlag;		// relative 0 at 12 o'clock, clockwise
		radRel = (radRel - (startAngle*dirFlag)).wrap(0, 2pi);	// relative to start position
		if (radRel.inRange(0, sweepLength)) {
			this.inputAction_(radRel/sweepLength); // triggers refresh
			stValue = value;
			stInput = input;
		};
	}

	/* Orientation and Movement */

	direction_ {|dir=\cw|
		direction = dir;
		dirFlag = switch (direction, \cw, {1}, \ccw, {-1});
		this.startAngle_(startAngle);
		this.sweepLength_(sweepLength);		// updates prSweepLength
		this.refresh;
	}

	startAngle_ {|radians=0|
		startAngle = radians;
		prStartAngle = -0.5pi + startAngle;		// start angle always relative to 0 is up, cw
		this.setPrCenter;
		this.ticksAtValues_(majTickVals, minTickVals, false);		// refresh the list of maj/minTicks positions
	}

	setPrCenter {
		prCenterAngle = -0.5pi + startAngle + (centerNorm*sweepLength*dirFlag);
		this.refresh;
	}

	centerValue_ {|value|
		centerValue = spec.constrain(value);
		centerNorm = spec.unmap(centerValue);
		this.setPrCenter;
	}

	sweepLength_ {|radians=2pi|
		sweepLength = radians;
		prSweepLength = sweepLength * dirFlag;
		this.setPrCenter;
		this.innerRadiusRatio_(innerRadiusRatio); // update innerRadiusRatio in case this was set to 0
		this.ticksAtValues_(majTickVals, minTickVals, false); // refresh the list of maj/minTicks positions
	}

	orientation_ {|vertHorizOrCirc = \vertical|
		orientation = vertHorizOrCirc;
	}

	innerRadiusRatio_ {|ratio|
		innerRadiusRatio = if (ratio == 0) {1e-5} {ratio};
		this.refresh
	}

	outerRadiusRatio_ {|ratio|
		outerRadiusRatio = ratio;
		this.refresh
	}

	bipolar_ {|bool|
		bipolar = bool;
		this.refresh;
	}

	// arrays of radian positions, reference from startAngle
	ticksAt_ {|majorRadPositions, minorRadPositions|
		majorRadPositions !? {
			majTicks = majorRadPositions;
			majTickVals = spec.map(majTicks / sweepLength);
		};
		minorRadPositions	!? {
			minTickVals = spec.map(minTicks / sweepLength);
			minTicks = minorRadPositions;
		};
		this.refresh;
	}

	// ticks at values unmapped by spec
	ticksAtValues_ {|majorVals, minorVals|
		majorVals !? {
			majTicks = spec.unmap(majorVals)*sweepLength;
			majTickVals = majorVals;
		};
		minorVals !? {
			minTicks = spec.unmap(minorVals)*sweepLength;
			minTickVals = minorVals;
		};
		this.refresh;
	}

	// ticks values by value hop, unmapped by spec
	ticksEveryVal_ {|valueHop, majorEvery=2|
		var num, ticks, numMaj, majList, minList;
		num = (spec.range / valueHop).floor.asInt;
		ticks = num.collect{|i| spec.unmap(i * valueHop) * sweepLength};
		numMaj = num/majorEvery;
		majList = List(numMaj);
		minList = List(num-numMaj);
		ticks.do{|val, i| if ((i%majorEvery) == 0) {majList.add(val)} {minList.add(val)} };
		this.ticksAt_(majList, minList);
		this.refresh;
	}


	ticksEvery_ {|radienHop, majorEvery=2|
		this.refresh;
	}

	// evenly distribute ticks
	numTicks_ {|num, majorEvery=2, endTick=true|
		var hop, ticks, numMaj, majList, minList;
		hop = if (endTick) {sweepLength / (num-1)} {sweepLength / num};
		ticks = num.asInt.collect{|i| i * hop};
		numMaj = num/majorEvery;
		majList = List(numMaj);
		minList = List(num-numMaj);
		ticks.do{|val, i| if ((i%majorEvery) == 0) {majList.add(val)} {minList.add(val)} };
		this.ticksAt_(majList, minList);
	}

	// extend superclass's spec setter to also update ticks
	// and centerValue in the case it's bipolar
	spec_ {|controlSpec, updateValue=true|
		super.spec_(controlSpec, updateValue);
		this.ticksAtValues_(majTickVals, minTickVals);
		this.centerValue_(centerValue);
	}
}
