package ComponentThemeDefinitions;

import java.util.HashMap;
import java.util.Map;

import com.googlecode.lanterna.graphics.Theme;
import com.googlecode.lanterna.graphics.ThemeDefinition;
import com.googlecode.lanterna.gui2.WindowDecorationRenderer;
import com.googlecode.lanterna.gui2.WindowPostRenderer;

public class gorgonDelegatingTheme implements Theme {
	private final Theme base;
	private final Map<Class<?>, ThemeDefinition> overrides = new HashMap<>();
	
	public gorgonDelegatingTheme(Theme base) {
		this.base = base;
	}
	
    public void override(Class<?> clazz, ThemeDefinition def) {
        overrides.put(clazz, def);
    }

	@Override
	public ThemeDefinition getDefaultDefinition() {
		return base.getDefaultDefinition();
	}

	@Override
	public ThemeDefinition getDefinition(Class<?> clazz) {
		 return overrides.getOrDefault(clazz, base.getDefinition(clazz));
	}

	@Override
	public WindowPostRenderer getWindowPostRenderer() {
		
		return base.getWindowPostRenderer();
	}

	@Override
	public WindowDecorationRenderer getWindowDecorationRenderer() {
		
		return base.getWindowDecorationRenderer();
	}

}
