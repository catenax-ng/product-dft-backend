package org.eclipse.tractusx.sde.common.utils;

import java.util.regex.Pattern;
import org.eclipse.tractusx.sde.common.exception.ValidationException;


public class SanitizationData {
	
	private static final Pattern zipPattern = Pattern.compile("^d{5}(-d{4})?$");
	
	public String sanatizeReqString(String data)
	{
		try {
			if(!data.matches("/^[-\\w\\s]+$/"))
			throw new ValidationException("Invalid Input for ShellId");
		}catch (Exception e) {
			e.printStackTrace();
		}
		return data;
	}
	
	
	public String validateString(String validateString) {

		if(!"urn:uuid:".contains(validateString))
    	{
    		throw new ValidationException("Invalid Input for ShellId");
    	}
    	return validateString;
	}
	
	
//  public String sanitizeInput(String string) {
//  return string.replaceAll("(?i)<script.*?>.*?</script.*?>", "") // case 1 - Open and close
//          .replaceAll("(?i)<script.*?/>", "") // case 1 - Open / close
//          .replaceAll("(?i)<script.*?>", "") // case 1 - Open and !close
//          .replaceAll("(?i)<.*?javascript:.*?>.*?</.*?>", "") // case 2 - Open and close
//          .replaceAll("(?i)<.*?javascript:.*?/>", "") // case 2 - Open / close
//          .replaceAll("(?i)<.*?javascript:.*?>", "") // case 2 - Open and !close
//          .replaceAll("(?i)<.*?\\s+on.*?>.*?</.*?>", "") // case 3 - Open and close
//          .replaceAll("(?i)<.*?\\s+on.*?/>", "") // case 3 - Open / close
//          .replaceAll("(?i)<.*?\\s+on.*?>", ""); // case 3 - Open and !close
//}

//public String sanitizeInputString(String string) {
//	 if (!string.match("[a-zA-Z0-9]")) {
//		    throw new Exception("Invalid username", string);
//		  }
//}
//// Define the policy.
//Function<HtmlStreamEventReceiver, HtmlSanitizer.Policy> policy
//  = new HtmlPolicyBuilder()
//      .allowElements("a", "p")
//      .allowAttributes("href").onElements("a")
//      .toFactory();
//

//private final HtmlPolicyBuilder DetailsPolicyBuilder = new HtmlPolicyBuilder()
//	    .allowElements("p", "ul", "ol", "li", "b", "i", "a")
//	    .allowStandardUrlProtocols()               // http, https, and mailto
//	    .allowAttributes("href").onElements("a");  // a tags can have an href element


}
