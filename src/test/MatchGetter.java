package test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import structures.CountryCompetition;
import structures.TimeVariations;
import basicStruct.CCAllStruct;
import basicStruct.MatchObj;
import extra.Status;
import extra.Unilang;

/**
 * @author Administrator Test Class simulating xscore
 */
public class MatchGetter {
	public static Logger logger = LoggerFactory.getLogger(MatchGetter.class);

	// public static List<MatchObj> schedNewMatches = new ArrayList<>();
	public static Map<Integer, List<MatchObj>> schedNewMatches = new HashMap<>();
	public static Map<Integer, List<MatchObj>> finNewMatches = new HashMap<>();
	public static Map<Integer, List<MatchObj>> errorNewMatches = new HashMap<>();
	// public static List<MatchObj> finNewMatches = new ArrayList<>();
	// public static List<MatchObj> errorNewMatches = new ArrayList<>();
	public static Map<Integer, List<MatchObj>> pendingMatches = new HashMap<>();

	// list of comp ids with all odds set. (to be used by odd adder sites)
	public static List<Integer> reviewedAndEmptyOdds = new ArrayList<Integer>();

	private final String mainUrl = "http://www.xscores.com/soccer/all-games/";
	public static LocalDate leatestLd = LocalDate.now();
	// private Unilang ul = new Unilang();
	private CountryCompetition cc = new CountryCompetition();

	private void scrapMatchesDate(LocalDate dat, String _status)
			throws IOException {
		/*
		 * get matches of the date from xScored.soccer; collect only matches
		 * based on the specified status. The matches are stored into their
		 * respective arrays
		 */
		String url = allDateFormater(dat);
		Document doc = null;
		try {
			logger.info("gettting url {}", url);
//			 doc = Jsoup.parse(new File( "C:/Users/Administrator/Desktop/score18fin.html"), "UTF-8");
			 
			doc = Jsoup .connect(url).userAgent( "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0").maxBodySize(0).timeout(600000).get();
			logger.info("Page Aquired!!");
		} catch (Exception e) {
			logger.info("couldnf connect or parse the page");
			e.printStackTrace();
		}

		Element tab = doc.select("table table table").get(2);
		Elements mrows = tab.getElementsByTag("tr");
		MatchObj mobj;
		for (Element row : mrows) {
			if (row.hasAttr("class") && row.attr("class").contains("#")) {
				String[] clasVal = row.attr("class").split("#");

				if (clasVal[0].contains("WORLD")
						|| clasVal[0].contains("AFRICA")
						|| clasVal[0].contains("AMERICA")
						|| clasVal[0].contains("EUROPE")
						|| clasVal[0].contains("ASIA")
						|| clasVal[4].contains("CUP")
						|| clasVal[4].contains("KUPA")
						|| clasVal[4].contains("COPPA")
						|| clasVal[4].contains("COPA")
						|| clasVal[4].contains("OFF")
						|| clasVal[4].contains("TROPHY")
						|| clasVal[4].contains("COUPE")
						|| clasVal[4].contains("EURO")) {
					continue;
				}
				int compId = searchForCompIdx(clasVal[0], clasVal[4]);
				if (compId < 0) {
					continue;
				} else {
					Elements tds = row.getElementsByTag("td");
					String matchTime = tds.get(0).text();
					String status = tds.get(1).text();
					String t1 = tds.get(5).text();
					String t2 = tds.get(9).text();
					mobj = new MatchObj();
					mobj.setT1(t1);
					mobj.setT2(t2);
					mobj.setMatchTime(matchTime);

					// mobj.setDat(Date.valueOf(dat));
					// mobj.setComId(compIdx + 1);
					// schedNewMatches.add(mobj);

					if (_status.equals(Status.SCHEDULED) && (status.equals(Status.SCHEDULED) || status .equals(Status.FTR))) {
						logger.info("getting SCHEDULED :: country {}  competition {}  compId {}  t1-{}  t2-{}", clasVal[0], clasVal[4], compId, t1, t2);
						mobj.setDat(Date.valueOf(dat));
						mobj.setComId(compId);
						// schedNewMatches.add(mobj);
						if (schedNewMatches.get(compId) == null) {
							List<MatchObj> mol = new ArrayList<>();
							mol.add(mobj);
							schedNewMatches.put(compId, mol);

							if (dat.isAfter(LocalDate.now())) {
								addToTomorrowComps(compId);
							}
						} else {
							schedNewMatches.get(compId).add(mobj);
							if (dat.isAfter(LocalDate.now())) {
								addToTomorrowComps(compId);
							}
						}
						if(dat.isAfter(leatestLd))
							leatestLd=dat;
						continue;
					}

					if (_status == Status.FINISHED) {
						if (status.equals(Status.ABANDONED) || status.equals(Status.CANCELED) 
								|| status.equals(Status.POSTPONED)) {
							// find interrupted matches to delete them
							mobj.setHt1(-1);
							mobj.setHt2(-1);
							mobj.setFt1(-1);
							mobj.setFt2(-1);							
							if (errorNewMatches.get(compId) == null) {
								List<MatchObj> mol = new ArrayList<>();
								mol.add(mobj);
								errorNewMatches.put(compId, mol);
							} else {
								errorNewMatches.get(compId).add(mobj);
							}
							// errorNewMatches.add(mobj); original, list version
							logger.info("ABANDONED ---  t1-{}  t2-{}", t1, t2);
						}// abandoned
						if (status.equals(Status.FINISHED)) {
							String[] scores;
							mobj.setDat(Date.valueOf(dat));
							mobj.setComId(compId);

							// HT score - @ td_14
							if (tds.get(13).text().length() >= 3) {// score-score
								scores = tds.get(13).text().split("-");
								try {// just in case some error happens
									int ht1 = Integer.parseInt(scores[0]);
									int ht2 = Integer.parseInt(scores[1]);
									mobj.setHt1(ht1);
									mobj.setHt2(ht2);
								} catch (NumberFormatException e) {
									logger.warn(" SOMTHING WHENT WRONG WITH THE HT SCORE");
									mobj.setHt1(-1);
									mobj.setHt2(-1);
								}
							}else{
								mobj.setHt1(-1);
								mobj.setHt2(-1);
							}
							if (tds.get(14).text().length() >= 3) {// FT @ td_15
								scores = tds.get(14).text().split("-");
								try {// just in case some error happens
									int ft1 = Integer.parseInt(scores[0]);
									int ft2 = Integer.parseInt(scores[1]);
									mobj.setFt1(ft1);
									mobj.setFt2(ft2);
								} catch (NumberFormatException e) {
									logger.warn(" SOMTHING WHENT WRONG WITH THE FT SCORE");
									mobj.setFt1(-1);
									mobj.setFt2(-1);
								}
							}else{
								mobj.setFt1(-1);
								mobj.setFt2(-1);
							}
							logger.info( "FINISHED ---  t1-{} vs t2-{} ;; {} , {}  ", t1, t2, tds.get(13).text(), tds.get(14) .text());
							if (finNewMatches.get(compId) == null) {
								List<MatchObj> mol = new ArrayList<>();
								mol.add(mobj);
								finNewMatches.put(compId, mol);
							} else {
								finNewMatches.get(compId).add(mobj);
							}
						}// fin status
					}
				}
			}
		}// for td

		if (dat.isEqual(LocalDate.now())) {
			addToTodayComps(); // add compId to todays list to be handled by R
		}
	}

	public void getFinishedOnDate(LocalDate dat) {
		// search for matches of that date that are finished (status
		// 'Fin'); `8`8```*~*~ get cancelled & postponed matches
		try {
			scrapMatchesDate(dat, Status.FINISHED);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void getScheduledOnDate(LocalDate dat) {
		// matches of that date that are scheduled(status Sched'); `*`* *
		try {
			scrapMatchesDate(dat, Status.SCHEDULED);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void getScheduledToday() {
		getScheduledOnDate(LocalDate.now());
	}

	public void getScheduledTomorrow() {
		getScheduledOnDate(LocalDate.now().plusDays(1));
	}

	public void getFinishedToday() {
		getFinishedOnDate(LocalDate.now());
	}

	public void getFinishedYesterday() {
		getFinishedOnDate(LocalDate.now().minusDays(1));
	}

	private String allDateFormater(LocalDate dat) {
		String url = mainUrl;
		String date = "";
		if (dat.getDayOfMonth() < 10) {
			date = "0" + dat.getDayOfMonth();
		} else {
			date = dat.getDayOfMonth() + "";
		}
		date = date + "-";
		if (dat.getMonthValue() < 10) {
			date = date + "0" + dat.getMonthValue();
		} else {
			date = date + dat.getMonthValue();
		}
		return mainUrl + date;

	}

	public void clearLists() {
		schedNewMatches.clear();
		finNewMatches.clear();
		errorNewMatches.clear();
	}

	private int searchForCompIdx(String country, String comp)
			throws IOException {

		/*
		 * cc allowed competitions should take a combination of country name and
		 * competition because many competitions have the same name but allways
		 * a unique combination of name&competition
		 */
		if (country.equals("SOUTH KOREA")) {
			logger.info("STOPING FOR KOREA");
		}
		if (cc.notAllowedcomps.contains(couComComb(country, comp))) {
			// if is one of the not allowed comps
			return -1;
		}

		Integer searchCompId = cc.allowedcomps.get(couComComb(country, comp));
		if (searchCompId != null) {// see if db ==1 -> available for processing

			Integer idx = CountryCompetition.idToIdx.get(searchCompId);
			if (cc.ccasList.get(idx).getDb() == 1) {
				return searchCompId;
			}
			return -1;
		} else {
			// search for country&comp in scorerDataStruct
			int searchCompIdx = cc.scorerCompIdSearch(country, comp);
			if (searchCompIdx > -1) {
				if (cc.sdsList.get(searchCompIdx).getDb() == 1) {
					return cc.sdsList.get(searchCompIdx).getCompId();
				}
			}
			// a rejected comp goes to the not allowed ones
			// cc.addNotAllowedComp(couComComb(country, comp));
			// logger.info("----{}  ", couComComb(country, comp));
			return searchCompIdx;
		}
	}

	private String couComComb(String country, String competition) {
		return country + "_" + competition;
	}

	// ////////////STORE & READ//////////////////
	public void storeSched() {
		File dir = new File("C:/m/");
		File sch = new File(dir + "/sch");
		if (!dir.exists()) {
			dir.mkdir();
		}
		try {
			ObjectOutputStream oos = new ObjectOutputStream(
					new FileOutputStream(sch));
			oos.writeObject(schedNewMatches);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void readSched() {
		File dir = new File("C:/m/");
		File sch = new File(dir + "/sch");
		if (!dir.exists()) {
			dir.mkdir();
		}

		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
					sch));
			schedNewMatches = (Map<Integer, List<MatchObj>>) ois.readObject();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// //////////////////////HELP FUNC/////////////
	public void addToTomorrowComps(int compId) {
		// get a list of competition ids that are to be played tomorrow
		if (!TimeVariations.tomorrowComps.contains(compId))
			TimeVariations.tomorrowComps.add(compId);
	}

	public void addToTodayComps() {
		/*
		 * since on strategy the todayMatches are the firs to be added all the
		 * data in the scheduled matches will be of the same day thas we get it
		 * all if the date is write
		 */
		TimeVariations.todayComps.addAll(MatchGetter.schedNewMatches.keySet());
	}

	// -----------------------------------------
	public void showPendingMmatches() {
		for (int keiCid : pendingMatches.keySet()) {
			logger.info("----------:::::::::-------{}", keiCid);
			for (int i = 0; i < pendingMatches.get(keiCid).size(); i++) {
				logger.info("{}", pendingMatches.get(keiCid).get(i)
						.printMatch());
			}
		}
	}

	public static void printPendingMmatchesSqlInsert() {
		for (int keiCid : pendingMatches.keySet()) {
			// logger.info("----------:::::::::-------{}",keiCid);
			for (int i = 0; i < pendingMatches.get(keiCid).size(); i++) {
				logger.info(
						"INSERT INTO tempmatches "
								+ "(mId,dat,matchTime,comId,t1,t2,ft1,ft2,ht1,ht2,_1,_2,_x,_o,_u) "
								+ "values", pendingMatches.get(keiCid).get(i)
								.printMatch());
			}
		}
	}

}
