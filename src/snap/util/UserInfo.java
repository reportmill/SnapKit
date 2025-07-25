package snap.util;
import snap.web.WebResponse;
import snap.web.WebURL;
import java.util.Objects;

/**
 * This class holds user info.
 */
public class UserInfo {

    // The user name
    private static String _userName;

    // The user email
    private static String _userEmail;

    // Constants for preference keys
    private static final String USER_NAME_KEY = "SnapUserName";
    private static final String USER_EMAIL_KEY = "SnapUserEmail";

    // The URL to post send mail request to
    private static String _sendMailUrl = "https://www.reportmill.com/cgi-bin/SendMail.py";

    /**
     * Initialize values.
     */
    static {
        _userName = Prefs.getDefaultPrefs().getString(USER_NAME_KEY, null);
        if (_userName == null)
            _userName = Prefs.getDefaultPrefs().getString("ExceptionUserName", "");
        _userEmail = Prefs.getDefaultPrefs().getString(USER_EMAIL_KEY, null);
        if (_userEmail == null)
            _userEmail = Prefs.getDefaultPrefs().getString("ExceptionEmail", "");
    }

    /**
     * Returns the user name.
     */
    public static String getUserName()  {  return _userName;  }

    /**
     * Sets the user name.
     */
    public static void setUserName(String aValue)
    {
        if (Objects.equals(aValue, getUserName())) return;
        _userName = aValue;
        Prefs.getDefaultPrefs().setValue(USER_NAME_KEY, _userName);
    }

    /**
     * Returns the user email.
     */
    public static String getUserEmail()  { return _userEmail;  }

    /**
     * Sets the user email address.
     */
    public static void setUserEmail(String aValue)
    {
        if (Objects.equals(aValue, getUserEmail())) return;
        _userEmail = aValue;
        Prefs.getDefaultPrefs().setValue(USER_EMAIL_KEY, _userEmail);
    }

    /**
     * Returns the user full email address.
     */
    public static String getFullUserEmail()
    {
        // Get from address
        String userName = getUserName();
        String userEmail = getUserEmail();
        if (userName.isEmpty() && userEmail.isEmpty())
            return "Anonymous";
        if (userName.isEmpty())
            return userEmail;
        if (userEmail.isEmpty())
            return userName;
        return userName + " <" + userEmail + '>';
    }

    /**
     * Sends an email with given from, to, subject, body and SendMail url.
     */
    public static String sendMail(String toAddr, String fromAddr, String aSubject, String aBody)
    {
        // Create full message text
        String messageText = String.format("To=%s\nFrom=%s\nSubject=%s\n%s", toAddr, fromAddr, aSubject, aBody);

        // Post message bytes to send mail url
        try {
            WebURL sendMailUrl = WebURL.getUrl(_sendMailUrl); assert (sendMailUrl != null);
            byte[] postBytes = messageText.getBytes();
            WebResponse resp = sendMailUrl.getResponseForPostBytes(postBytes);
            if (resp.getCode() == WebResponse.OK)
                return "Success!";
            return resp.getErrorString();
        }

        // Complain
        catch (Exception e) { return e.getMessage(); }
    }
}
