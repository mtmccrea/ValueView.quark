XYScaleView : ValuesView {
	var <>moveRelative, <>fixSquare = true;
	var background, range, levels;
	var <bnds, <cen, <minDim, <canvas; // for access by drawing layers
	var mDownInCanvas, stInputs;

	*new {
		|parent, bounds, specs, initVals, moveRelative=false|
		^super.new(parent, bounds, specs, initVals).init(moveRelative);
	}


	init { |argMoveRelative|
		moveRelative = argMoveRelative;

		// REQUIRED: in subclass init, initialize drawing layers
		// initialize layer classes and save them to vars
		#range, levels = [
			XYBackgroundLayer, XYRangeLayer, XYLevelsLayer
		].collect({
			|class|
			class.new(this, class.properties)
		});
		// convenience variable to access a list of the layers
		layers = [background, range, levels];

		this.defineMouseActions;
	}

	drawFunc {
		^{|v|
			// "global" instance vars, accessed by ValueViewLayers
			bnds = v.bounds;
			cen  = bnds.center;
			minDim = min(bnds.width, bnds.height);
			canvas = if (fixSquare) {
				Size(minDim, minDim).asRect.center_(cen);
			} {
				v.bnds.size.asRect
			};
			this.drawInThisOrder;
		};
	}

	drawInThisOrder {
		// tranlate to top left of drawing area
		Pen.translate(canvas.leftTop.x, canvas.leftTop.y);
		if (background.p.show) {background.fill};
		if (range.p.show) {range.stroke};
		if (levels.p.show) {levels.stroke};
		if (background.p.show) {background.stroke};

	}

	defineMouseActions {

		// assign action variables: down/move
		mouseDownAction = {
			|v, x, y|
			mouseDownPnt = x@y; // set for moveAction; = x@y;
			mDownInCanvas = canvas.containsPoint(mouseDownPnt);
			if (mDownInCanvas) {
				if (moveRelative.not) {
					this.setAbsPosInput(mouseDownPnt);
				} {
					stInputs = inputs;
				}
			}
		};

		mouseMoveAction = {
			|v, x, y|
			mouseMovePnt = x@y;
			if (mDownInCanvas) { // make sure click started in canvas bounds
				if (moveRelative) {
					var dx, dy; // normalized change
					dx = mouseMovePnt.x - mouseDownPnt.x / canvas.width;
					dy = (mouseMovePnt.y - mouseDownPnt.y / canvas.height).neg;
					this.inputsAction_(dx - stInputs[0], dy - stInputs[1]);
				} { // folow absolute position
					this.setAbsPosInput(mouseMovePnt);
				}
			}
		};

	}


	setAbsPosInput { |mPnt|
		var normX, normY, pxRel;
		pxRel = mPnt - canvas.leftTop;
		normX = pxRel.x/canvas.width;
		normY = (canvas.height - pxRel.y)/canvas.height;
		this.inputsAction_(normX, normY);
	}

}