package ComponentThemeDefinitions;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.AbstractComponent;
import com.googlecode.lanterna.gui2.ComponentRenderer;
import com.googlecode.lanterna.gui2.TextGUIGraphics;

public class Patterned_backdrops extends AbstractComponent<Patterned_backdrops> {
	
	private String[] pattern;

    
    public Patterned_backdrops() {
    	pattern =generateZigzagPattern(100, 100);
    }
	@Override
	protected ComponentRenderer<Patterned_backdrops> createDefaultRenderer() {

		return new ComponentRenderer<Patterned_backdrops>() {
			
			@Override
			public TerminalSize getPreferredSize(Patterned_backdrops component) {
				return TerminalSize.ONE;
			}
			
			@Override
			public void drawComponent(TextGUIGraphics graphics, Patterned_backdrops component) {
                TerminalSize size = graphics.getSize();
                TextColor.RGB backgroundColor  = new TextColor.RGB(16, 74, 57);
                TextColor.RGB foregroundColor = new TextColor.RGB(212, 175, 55);
                graphics.setForegroundColor(blend(backgroundColor,foregroundColor,0.3));
                graphics.setBackgroundColor(backgroundColor);
				
                int numRows = pattern.length;

                for (int y = 0; y < size.getRows(); y++) {
                    String row = pattern[y % numRows];
                    int rowLen = row.length();
                    if (rowLen == 0) continue;
                    for (int x = 0; x < size.getColumns(); x++) {
                        char c = row.charAt(x % rowLen);
                        graphics.setCharacter(x, y, c);
                    }
                }
			}
		};
	}
	

	public static String[] generateZigzagPattern(int rowCount, int repeats) {

		String[] units = {"/\\  ", "/  \\", "\\  /", "\\/  "};
		String[] leads = {" ", "", "", " "};

		String[] pattern = new String[rowCount];
		for (int y = 0; y < rowCount; y++) {
		    int phase = y % 4;
		    pattern[y] = leads[phase] + units[phase].repeat(repeats);
		}
		return pattern;
	}
	public static TextColor.RGB blend(TextColor.RGB pattern, TextColor.RGB base, double opacity) {
	    // opacity: 1.0 = fully pattern color, 0.0 = fully base color
	    int r = (int) Math.round(pattern.getRed()   * opacity + base.getRed()   * (1 - opacity));
	    int g = (int) Math.round(pattern.getGreen() * opacity + base.getGreen() * (1 - opacity));
	    int b = (int) Math.round(pattern.getBlue()  * opacity + base.getBlue()  * (1 - opacity));
	    return new TextColor.RGB(r, g, b);
	}
	
	

}

