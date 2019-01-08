XYRangeLayer : ValueViewLayer {

	*properties {
		^(
			show:            true,
			strokeXColor:    Color.blue.alpha_(0.5),
			strokeYColor:    Color.red.alpha_(0.5),
			strokeWidth:     0.03, // if style: \wedge, if < 1, assumed to be a normalized value and changes with view size, else treated as a pixel value
			showRangeLabels: true,
			fontName:        "Helvetica",
			fontSize:        0.07, // pixels or relative to smallest view dimension
		)
	}

	stroke {
		var strokeWidth, sw_h, txtRect, tmp, w, h, font, str1, str2;
		strokeWidth = if (p.strokeWidth<1){p.strokeWidth*view.minDim}{p.strokeWidth};
		sw_h = strokeWidth * 0.5;
		w = view.canvas.width;
		h = view.canvas.height;

		Pen.push;
		// bottom axis
		Pen.strokeColor_(p.strokeXColor);
		Pen.width = strokeWidth;
		tmp = h-sw_h;
		Pen.moveTo(0@tmp);
		Pen.lineTo(w@tmp);
		Pen.stroke;
		if (p.showRangeLabels) {
			str1 = view.specs[0].minval.asString;
			str2 = view.specs[0].maxval.asString;
			font = Font(
				p.fontName,
				if (p.fontSize < 1) { p.fontSize * view.minDim } { p.fontSize }
			);
			txtRect = (str1++str2).bounds(font);

			// txt labels
			tmp = h-strokeWidth;
			Pen.stringInRect(
				str1,
				txtRect.left_(0).bottom_(tmp),
				font, p.strokeXColor, QAlignment(\left)
			);
			Pen.stringInRect(
				str2,
				txtRect.right_(w-strokeWidth).bottom_(h-strokeWidth-txtRect.height),
				font, p.strokeXColor, QAlignment(\right)
			);
		};

		// right axis
		Pen.strokeColor_(p.strokeYColor);
		tmp = w-sw_h;
		Pen.moveTo(tmp@h);
		Pen.lineTo(tmp@0);
		Pen.stroke;
		if (p.showRangeLabels) {
			str1 = view.specs[1].minval.asString;
			str2 = view.specs[1].maxval.asString;
			// txt labels
			Pen.stringInRect(
				str1,
				txtRect.right_(w-strokeWidth).bottom_(h-strokeWidth),
				font, p.strokeYColor, QAlignment(\right)
			);
			Pen.stringInRect(
				str2,
				txtRect.right_(w-strokeWidth).top_(0),
				font, p.strokeYColor, QAlignment(\right)
			);
		};
		Pen.pop;
	}

	fill {}
}

XYLevelsLayer : ValueViewLayer {

	*properties {
		^(
			show:           true,
			showCrosshairs: true,
			showPoint:      true,
			pointSize:      0.05,
			pointColor:     nil, // nil blends strokex and strokey colors
			showText:       true,
			strokeXColor:   Color.blue.alpha_(0.5),
			strokeYColor:   Color.red.alpha_(0.5),
			strokeWidth:    0.01,  // if style: \wedge, if < 1, assumed to be a normalized value and changes with view size, else treated as a pixel value
			fontName:       "Helvetica",
			fontSize:       0.07, // pixels or relative to smallest view dimension
			round:          0.1,
		)
	}

	stroke {
		var strokeWidth, txtSize, c, xpx, ypx, psize;
		var str1, str2, txtRect, txtRect1, txtRect2, font, align;

		strokeWidth = if (p.strokeWidth < 1) {
			p.strokeWidth * view.minDim
		} {
			p.strokeWidth
		};
		c = view.canvas;
		#xpx, ypx = view.inputs * [c.width, c.height];
		ypx = c.height - ypx; // invert y for upward positive

		Pen.push;

		if (p.showCrosshairs) {
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
		};

		if (p.showPoint) {
			psize = if (p.pointSize < 1) { p.pointSize * view.minDim } { p.pointSize * 2 };
			Pen.fillColor_(
				p.pointColor ?? {p.strokeXColor.blend(p.strokeYColor, 0.5)}
			);
			Pen.fillOval(Size(psize, psize).asRect.center_(xpx@ypx));
		};

		if (p.showText) {
			str1 = view.values[0].round(p.round).asString;
			str2 = view.values[1].round(p.round).asString;
			font = Font(
				p.fontName,
				if (p.fontSize < 1) { p.fontSize * view.minDim } { p.fontSize }
			);
			txtRect = [str1, str2].at(
				[str1.size, str2.size].maxIndex
			).bounds(font);

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
			Pen.stringInRect(str1, txtRect1, font, p.strokeXColor, QAlignment(align));
			// y value
			Pen.stringInRect(str2, txtRect2, font, p.strokeYColor, QAlignment(align));
		};

		Pen.pop;
	}

	fill {}
}

XYBackgroundLayer : ValueViewLayer {

	*properties {
		^(
			show:        true,
			stroke:      false,
			strokeColor: Color.black,
			strokeWidth: 2,  // if style: \wedge, if < 1, assumed to be a normalized value and changes with view size, else treated as a pixel value
			fill:        true,
			fillColor:   Color(0.9, 0.9, 0.9),
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
			Pen.strokeRect(
				Rect(sw_h, sw_h, c.width-strokeWidth, c.height-strokeWidth) //.center_((view.bnds.width/2)@(view.bnds.height/2))
			);
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