package api.functionality;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import api.functionality.obj.CountryCompCompId;

/**
 * @author Administrator
 *
 *         the comm0on adversaries will keep the data from the last 10 weekly
 *         matches and the curent week matches data like form, atack,defence,
 *         avg score, scores etc. it will support two versions the full and red
 *         acording to the page all or specific. The data will be send as csv,
 *         text and parsed with papa parser
 */

public class CommonAdversariesHandler {
	public static Logger log = LoggerFactory
			.getLogger(CommonAdversariesHandler.class);
	
	public static Map<Integer,String> commonAdv = new HashMap<>();

	
}
