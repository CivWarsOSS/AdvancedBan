package me.leoko.advancedban.manager;

import java.util.ArrayList;
import java.util.List;

import me.leoko.advancedban.MethodInterface;
import me.leoko.advancedban.Universal;

/**
 * Created by Leoko @ dev.skamps.eu on 13.07.2016.
 */
public class MessageManager {

	private static final MethodInterface mi = Universal.get().getMethods();

	public static String getMessage(String path, String... parameters) {
		String str = mi.getString(mi.getMessages(), path);
		if (str == null) {
			str = "Failed! See console for details!";
			Universal.get().log("!! Message-Error!\n" + "In order to solve the problem please:"
					+ "\n  - Check the Message-File for any missing or double \" or '"
					+ "\n  - Delete the message file and restart the server");
		} else {
			str = replace(str, parameters).replace('&', '§');
		}
		return str;
	}

	public static List<String> getLayout(Object file, String path, String... parameters) {
		if (mi.contains(file, path)) {
			List<String> list = new ArrayList<>();
			for (String str : mi.getStringList(file, path)) {
				list.add(replace(str, parameters).replace('&', '§'));
			}
			return list;
		} else {
			Universal.get().log(
					"!! Message-Error in " + mi.getFileName(file) + "!\n" + "In order to solve the problem please:"
							+ "\n  - Check the " + mi.getFileName(file) + "-File for any missing or double \" or '"
							+ "\n  - Delete the message file and restart the server");
			return null;
		}
	}

	public static List<String> getLayout(String path, String... parameters) {
		return getLayout(mi.getLayouts(), path, parameters);
	}

	public static void sendMessage(Object sender, String path, boolean prefix, String... parameters) {
		mi.sendMessage(sender,
				(prefix && !mi.getBoolean(mi.getConfig(), "Disable Prefix", false) ? getMessage("General.Prefix") + " "
						: "") + getMessage(path, parameters));
	}

	private static String replace(String str, String... parameters) {
		for (int i = 0; i < parameters.length - 1; i = i + 2) {
			str = str.replaceAll("%" + parameters[i] + "%", parameters[i + 1]);
		}
		return str;
	}
}
