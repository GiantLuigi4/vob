package tfc.vob.instancing.yaml;

import java.util.*;

public class Hsml extends HsmlEntry {
	public Map<String, HsmlEntry> entries = new HashMap<>();
	public Set<String> orderedKeys = new LinkedHashSet<>();
	
	public Set<String> keys() {
		return orderedKeys;
	}
	
	public Hsml getYaml(String key) {
		try {
			return (Hsml) entries.get(key);
		} catch (Throwable err) {
			System.out.println("Failed to get entry " + key);
			throw new RuntimeException();
		}
	}
	
	public HsmlEntry getEntry(String key) {
		return entries.get(key);
	}
	
	public String getText(String key) {
		return entries.get(key).toString();
	}
	
	public static int countIndent(String line) {
		for (int i = 0; i < line.length(); i++) {
			if (line.charAt(i) > ' ')
				return i;
		}
		return line.length();
	}
	
	protected ArrayList<String> csv(String src) {
		char inString = ' ';
		boolean iesc = false;
		ArrayList<String> text = new ArrayList<>();
		StringBuilder textBuilder = new StringBuilder();
		
		for (char c : src.toCharArray()) {
			if (!iesc && inString != ' ' && c == inString) {
				inString = ' ';
			} else if (!iesc && (c == '"' || c == '\'')) {
				inString = c;
			}
			
			iesc = c == '\\' && !iesc;
			
			if (c == '*' && inString == ' ') {
				text.add(textBuilder.toString());
				textBuilder = new StringBuilder();
			} else {
				textBuilder.append(c);
			}
		}
		
		if (textBuilder.length() != 0) text.add(textBuilder.toString());
		
		return text;
	}
	
	public Hsml(String[] text, int indent, int[] currentLine) {
		for (int i = currentLine[0]; i < text.length; i++) {
			if (text[i].trim().isEmpty()) continue;
			if (text[i].trim().charAt(0) == '#') continue;
			
			if (countIndent(text[i]) < indent) {
				currentLine[0] = i;
				return;
			}
			
			List<String> strings;
			if (text[i].contains("*")) strings = csv(text[i]);
			else strings = Collections.singletonList(text[i]);
			
			for (String entry : strings) {
				String[] split = entry.trim().split(":", 2);
				if (split.length == 1 || split[1].isEmpty()) {
					i += 1;
					currentLine[0] = i;
					Hsml subYaml = new Hsml(text, countIndent(text[i]), currentLine);
					i = currentLine[0] - 1;
					
					entries.put(split[0], subYaml);
				} else entries.put(split[0], new HsmlString(split[1].trim()));
				
				orderedKeys.add(split[0]);
			}
			
			currentLine[0] = i;
		}
		currentLine[0] += 1;
	}
	
	public Hsml(String text) {
		this(text.split("\n"), 0, new int[]{0});
	}
}