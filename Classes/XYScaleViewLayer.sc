XYRangeLayer : ValueViewLayer {

	*properties {
		^(
			show:					true,
			strokeXColor: Color.blue.alpha_(0.5),
			strokeYColor: Color.red.alpha_(0.5),
			strokeWidth: 	0.03,						// if style: \wedge, if < 1, assumed to be a normalized value and changes with view size, else treated as a pixel value
			fontSize:			11,
		)
	}

	stroke {
		var strokeWidth, sw_h, txtRect, tmp, w, h, font;
		strokeWidth = if (p.strokeWidth<1){p.strokeWidth*view.minDim}{p.strokeWidth};
		sw_h = strokeWidth * 0.5;
		txtRect = Size(45,p.fontSize+2).asRect;
		w = view.canvas.width;
		h = view.canvas.height;
		font = Font("Helvetica", p.fontSize);

		Pen.push;
		// bottom axis
		Pen.strokeColor_(p.strokeXColor);
		Pen.width = strokeWidth;
		tmp = h-sw_h;
		Pen.moveTo(0@tmp);
		Pen.lineTo(w@tmp);
		Pen.stroke;
		// txt labels
		tmp = h-strokeWidth;
		Pen.stringInRect(
			view.specs[0].minval.asString,
			txtRect.left_(0).bottom_(tmp),
			font, p.strokeXColor, QAlignment(\left)
		);
		Pen.stringInRect(
			view.specs[0].maxval.asString,
			txtRect.right_(w-strokeWidth).bottom_(h-strokeWidth-txtRect.height),
			font, p.strokeXColor, QAlignment(\right)
		);

		// right axis
		Pen.strokeColor_(p.strokeYColor);
		tmp = w-sw_h;
		Pen.moveTo(tmp@h);
		Pen.lineTo(tmp@0);
		Pen.stroke;
		// txt labels
		Pen.stringInRect(
			view.specs[1].minval.asString,
			txtRect.right_(w-strokeWidth).bottom_(h-strokeWidth),
			font, p.strokeYColor, QAlignment(\right)
		);
		Pen.stringInRect(
			view.specs[1].maxval.asString,
			txtRect.right_(w-strokeWidth).top_(0),
			font, p.strokeYColor, QAlignment(\right)
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
			fontSize:			14,
		)
	}

	stroke {
		var strokeWidth, txtSize, c, xpx, ypx;
		var txtRect, txtRect1, txtRect2, font, align;
		strokeWidth = if (p.strokeWidth<1){p.strokeWidth*view.minDim}{p.strokeWidth};
		txtSize = Size(45@25);
		c = view.canvas;
		#xpx, ypx = view.inputs * [c.width, c.height];
		ypx = c.height - ypx; // invert y for upward positive
		font = Font("Helvetica", p.fontSize);

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

		Pen.fillColor_(p.strokeXColor.blend(p.strokeYColor, 0.5));
		Pen.fillOval(Size(6,6).asRect.origin_(xpx@ypx-3@3));

		if (p.showText) {
			txtRect = Size(45,p.fontSize+2).asRect;
			// find left bound
			if (xpx < txtRect.width) {
				txtRect = txtRect.left_(xpx+4);
				align = \left;
			} {
				txtRect = txtRect.right_(xpx-4);
				align = \right;
			};
			// find top bound
			if (ypx < (txtRect.height*2)) {
				txtRect1 = txtRect.copy.top_(ypx+2); // x val rect
				txtRect2 = txtRect.top_(ypx+txtRect.height); // y val rect
			} {
				txtRect1 = txtRect.copy.bottom_(ypx-txtRect.height); // x val rect
				txtRect2 = txtRect.bottom_(ypx-2); // y val rect
			};
			// x value
			Pen.stringInRect(
				view.values[0].round(0.1).asString, txtRect1,
				font, p.strokeXColor, QAlignment(align)
			);
			// y value
			Pen.stringInRect(
				view.values[1].round(0.1).asString, txtRect2,
				font, p.strokeYColor, QAlignment(align)
			);
		};

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
			fillColor:		Color(0.9, 0.9, 0.9),
		)
	}

	stroke {
		var strokeWidth, sw_h, c;
		if (p.stroke) {
			strokeWidth = if (p.strokeWidth<1){p.strokeWidth*view.minDim}{p.strokeWidth};
			sw_h = strokeWidth * 0.5;
			c = view.canvas;
			Pen.push;
			Pen.strokeColor_(p.strokeXColor);
			Pen.width_(strokeWidth);
			Pen.strokeRect(Size(c.width-sw_h, c.height-sw_h).asRect.top_(c.top+sw_h).left_(c.left+sw_h));
			Pen.pop;
		}
	}

	fill {
		if (p.fill) {
			Pen.push;
			Pen.fillColor_(p.fillColor);
			Pen.fillRect(Rect(0,0,view.canvas.width, view.canvas.height));
			Pen.pop;
		}
	}
}