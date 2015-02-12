package thenoob.blackbox.joystickdroid;
import android.net.ParseException;

public class Utility {
	public static int VK_ENTER = 10 ;
	public static int VK_SHIFT = 16;
	public static int VK_CTRL = 17;
	public static int VK_ALT = 18;
	public static int VK_ESC = 27;
	public static int VK_TAB = 9; 
	public static int VK_CAPSLOCK = 20;
	public static int VK_UP = 37;
	public static int VK_RIGHT = 38;
	public static int VK_DOWN = 39;
	public static int VK_LEFT = 40;
	public static int VK_PAGEUP = 33;
	public static int VK_PAGEDOWN = 34;
	public static int getparsedInt(String number) {
		int n = 1;
		try {
			n = Integer.parseInt(number);
		}
		catch(ParseException e) {
			e.printStackTrace();
			return (Integer) null;
		}
		return n;
	}
}
