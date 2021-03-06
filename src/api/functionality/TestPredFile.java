package api.functionality;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


//import api.functionality.obj.MatchSpecificDescriptionData;
import basicStruct.StrStrTuple;
import diskStore.AnalyticFileHandler;

public class TestPredFile implements CsvFileHandler {

	public static Logger log = LoggerFactory.getLogger(TestPredFile.class);
	private int lastRecWeek;
	private int linesRead = 0;

	public List<StrStrTuple> daylyAdversaries(int compId, String compName, String country) {
		/*
		 * parse the csv file specified by the parameters and collect and return
		 * the adversary teams playing today
		 */
		CSVParser parser = parser(compId, compName, country);

		List<StrStrTuple> daylyMatchAdversaries = new ArrayList<>();
		StrStrTuple temp;
		for (CSVRecord record : parser) {
			temp = new StrStrTuple();
			log.info("{}  -  {}", record.get("t1"), record.get("t2"));
			temp.setS1(record.get("t1"));
			temp.setS2(record.get("t2"));
			daylyMatchAdversaries.add(temp);
		}
		return (daylyMatchAdversaries);
	}

	@Override
	public String fullCsv(int compId, String compName, String country, LocalDate ld, String homeTeam, String awayTeam) {
		/*
		 * read data for match specific page. so get the data only for the two
		 * teams provided
		 */
		CSVParser parser = parser(compId, compName, country);
		if (parser == null)
			return null;
		StringBuilder sb = new StringBuilder();
		for (CSVRecord record : parser) {
			
			if (record.get("t1").equals(homeTeam) || record.get("t2").equals(homeTeam)
					|| record.get("t1").equals(awayTeam) || record.get("t2").equals(awayTeam)) {
				linesRead++;
				sb.append(record.get("week") + "," + record.get("t1") + "," + record.get("t2") + "," + (-1) + "," + (-1)
						+ "," + (-1) + "," + (-1) + "," + record.get("t1Form") + "," + record.get("t2Form") + ","
						+ record.get("t1AtackIn") + "," + record.get("t1AtackOut") + "," + record.get("t2AtackIn") + ","
						+ record.get("t2AtackOut") + "," + record.get("t1DefenseIn") + "," + record.get("t1DefenseOut")
						+ "," + record.get("t2DefenseIn") + "," + record.get("t2DefenseOut") + "\n");
			}
		}
		return sb.toString();
	}

	public String reducedCsv(int compId, String compName, String country, LocalDate ld) {
		CSVParser parser = parser(compId, compName, country, ld);
		if (parser == null)
			return null;
		StringBuilder sb = new StringBuilder();
		for (CSVRecord record : parser) {
			linesRead++;
			// adaptLastRecWeek(record.get("week"));
			sb.append(record.get("week") + "," + record.get("t1") + "," + record.get("t2") + "," + (-1) + "," + (-1)
					+ "," + record.get("t1Form") + "," + record.get("t2Form") + "," + record.get("t1Atack") + ","
					+ record.get("t2Atack") + "," + "," + record.get("t1Defense") + "," + record.get("t2Defense")
					+ "\n");
		}

		return sb.toString();
	}

	private CSVParser parser(int compId, String compName, String country, LocalDate ld) {
		CSVFormat format = CSVFormat.RFC4180.withHeader();

		AnalyticFileHandler afh = new AnalyticFileHandler();
		CSVParser parser = null;
		try {
			File f = afh.getTestFileName(compId, compName, country, ld);
			if (f != null)
				parser = new CSVParser(new InputStreamReader(new   FileInputStream(f),StandardCharsets.UTF_8), format);
//				parser = new CSVParser(new FileReader(f), format);
			log.info("file {} {} not found", compName, country);
		} catch (IOException e) {
			log.warn("Parsing exception");
			e.printStackTrace();
		}
//		log.info(parser.toString());
		return parser;
	}

	private CSVParser parser(int compId, String compName, String country) {
		/* get the leatest (today or in future test pred files and reads it) */
		CSVFormat format = CSVFormat.RFC4180.withHeader();

		AnalyticFileHandler afh = new AnalyticFileHandler();
		CSVParser parser = null;
		try {
			File f = afh.getLeatestTestFileName(compId, compName, country);
			if (f != null)
				parser = new CSVParser(new InputStreamReader(new   FileInputStream(f),StandardCharsets.UTF_8), format);
//				parser = new CSVParser(new FileReader(f), format);
			log.info("file {} {} not found", compName, country);
		} catch (IOException e) {
			log.warn("Parsing exception");
			e.printStackTrace();
		}
		log.info(parser.toString());
		return parser;
	}

	public int getLastRecWeek() {
		return lastRecWeek;
	}

	public void setLastRecWeek(int lastRecWeek) {
		this.lastRecWeek = lastRecWeek;
	}

	private void adaptLastRecWeek(String week) {
		lastRecWeek = Integer.parseInt(week);
	}

	public int getLinesRead() {
		return linesRead;
	}

	@Override
	public String reducedCsv(int compId, String compName, String country) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String fullCsv(int compId, String compName, String country) {
		// TODO Auto-generated method stub
		return null;
	}


}
