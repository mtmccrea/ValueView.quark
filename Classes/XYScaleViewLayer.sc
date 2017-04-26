XYRangeLayer : ValueViewLayer {

	*properties {
		^(
			show:					true,
			strokeXColor: Color.blue.alpha_(0.5),
			strokeYColor: Color.red.alpha_(0.5),
			strokeWidth: 	2,						// if style: \wedge, if < 1, assumed to be a normalized value and changes with view size, else treated as a pixel value
			fontSize:			11,
		)
	}

	stroke {
		var strokeWidth, sw_h, txtSize, c;
		strokeWidth = if (p.strokeWidth<1){p.strokeWidth*view.minDim}{p.strokeWidth};
		sw_h = strokeWidth * 0.5;
		txtSize = Size(45@25);
		c = view.canvas;

		Pen.push;
		// bottom axis
		Pen.strokeColor_(p.strokeXColor);
		Pen.moveTo(c.leftBottom - (0@sw_h));
		Pen.lineTo(c.rightBottom - (0@sw_h));
		Pen.stroke;
		// txt labels
		Pen.stringLeftJustIn(
			view.specs[0].minval.asString,
			txtSize.asRect.left_(c.left).bottom_(c.bottom - strokeWidth),
			Font.default.size_(p.fontSize),
			p.strokeXColor
		);
		Pen.stringRightJustIn(
			view.specs[0].maxval.asString,
			txtSize.asRect.right_(c.right-strokeWidth).bottom_(c.bottom - strokeWidth),
			Font.default.size_(p.fontSize),
			p.strokeXColor
		);

		// right axis
		Pen.strokeColor_(p.strokeYColor);
		Pen.moveTo(view.c.rightBottom - (sw_h@0));
		Pen.lineTo(view.c.rightTop - (sw_h@0));
		Pen.stroke;
		// txt labels
		Pen.stringRightJustIn(
			view.specs[1].minval.asString,
			txtSize.asRect.right_(c.right-strokeWidth).bottom_(c.bottom - strokeWidth - p.fontSize - 5),
			Font.default.size_(p.fontSize),
			p.strokeYColor
		);
		Pen.stringRightJustIn(
			view.specs[1].maxval.asString,
			txtSize.asRect.right_(c.right-strokeWidth).top_(c.top),
			Font.default.size_(p.fontSize),
			p.strokeYColor
		);

		Pen.pop;
	}

	fill {}
}

XYLevelsLayer : ValueViewLayer {

	*properties {
		^(
			show:					true,
			showText:			true,
			strokeXColor: Color.blue.alpha_(0.5),
			strokeYColor: Color.red.alpha_(0.5),
			strokeWidth: 	2,						// if style: \wedge, if < 1, assumed to be a normalized value and changes with view size, else treated as a pixel value
			fontSize:			11,
		)
	}

	stroke {
		var strokeWidth, txtSize, c, xpx, ypx;
		strokeWidth = if (p.strokeWidth<1){p.strokeWidth*view.minDim}{p.strokeWidth};
		txtSize = Size(45@25);
		c = view.canvas;
		#xpx, ypx = view.inputs * [c.width, c.height];
		ypx = c.height - ypx; // invert y for upward positive

		Pen.push;
		// x value
		Pen.strokeColor_(p.strokeXColor);
		Pen.moveTo(xpx@c.height);
		Pen.lineTo(xpx@0);
		Pen.stroke;
		// y value
		Pen.strokeColor_(p.strokeYColor);
		Pen.moveTo(0@ypx);
		Pen.lineTo(c.width@ypx);
		Pen.stroke;

		Pen.pop;
	}

	fill {}
}

XYBackgroundLayer : ValueViewLayer {

	*properties {
		^(
			show:					true,
			stroke:				false,
			strokeColor:	Color.black,
			strokeWidth: 	2,						// if style: \wedge, if < 1, assumed to be a normalized value and changes with view size, else treated as a pixel value
			fill:					true,
			fillColor:		Color(0.1, 0.1, 0.1),
		)
	}

	stroke {
		var strokeWidth, sw_h, c;
		strokeWidth = if (p.strokeWidth<1){p.strokeWidth*view.minDim}{p.strokeWidth};
		sw_h = strokeWidth * 0.5;
		c = view.canvas;
		Pen.push;
		Pen.strokeColor_(p.strokeXColor);
		Pen.width_(strokeWidth);
		Pen.strokeRect(Size(c.width-sw_h, c.height-sw_h).asRect.top_(c.top+sw_h).left_(c.left+sw_h));
		Pen.pop;
	}

	fill {
		Pen.push;
		Pen.fillColor_(p.fillColor);
		Pen.fillRect(view.canvas);
		Pen.pop;
	}
}