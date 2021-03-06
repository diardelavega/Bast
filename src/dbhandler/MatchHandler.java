package dbhandler;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import dbtry.Conn;
import basicStruct.MatchObj;

/**
 * @author Administrator
 *
 *         a class that would allow the db interaction (insert and retrieve) of
 *         MatchObj
 */
public class MatchHandler {

	List<MatchObj> mlist = new ArrayList<>();
	Conn conn;

	private void getCoonection() {
		conn = new Conn();
		conn.open();
	}

	public void insertMatches(List<MatchObj> mlist) throws SQLException {
		getCoonection();
		String insert = "insert into matches values(null,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		PreparedStatement ps = conn.getConn().prepareStatement(insert);

		for (MatchObj mobj : mlist) {
			ps.setInt(1, mobj.getComId());
			ps.setString(2, mobj.getT1());
			ps.setString(3, mobj.getT2());
			ps.setInt(4, mobj.getFt1());
			ps.setInt(5, mobj.getFt2());
			ps.setInt(6, mobj.getHt1());
			ps.setInt(7, mobj.getHt2());
			ps.setDouble(8, mobj.get_1());
			ps.setDouble(9, mobj.get_x());
			ps.setDouble(10, mobj.get_2());
			ps.setDouble(11, mobj.get_o());
			ps.setDouble(12, mobj.get_u());
			ps.setDate(13, mobj.getDat());
			ps.addBatch();
		}
		ps.executeBatch();
		ps.close();
		conn.close();

	}

	public void getMatches() throws SQLException {
		getCoonection();
		Statement st = conn.getConn().createStatement();
		ResultSet rs = st.executeQuery("SELECT * FROM matches");
		listFill(rs);
		rs.close();
		st.close();
		conn.close();

	}

	public void getMatchesComp(int compId) throws SQLException {
		getCoonection();
		Statement st = conn.getConn().createStatement();
		ResultSet rs = st.executeQuery("SELECT * FROM matches WHERE compid = "
				+ compId);
		listFill(rs);
		rs.close();
		st.close();
		conn.close();
	}

	public void getMatchesDateBetween(Date date1, Date date2)
			throws SQLException {
		getCoonection();
		// the lower date rows are included, the upper date rows are not
		// included
		Statement st = conn.getConn().createStatement();
		ResultSet rs = st
				.executeQuery("SELECT * FROM matches WHERE dat between "
						+ date1 + " AND " + date2);
		listFill(rs);
		rs.close();
		st.close();
		conn.close();
	}

	public void getMatchesDateComp(int compId, Date date1, Date date2)
			throws SQLException {
		getCoonection();
		// the lower date rows are included, the upper date rows are not
		// included
		Statement st = conn.getConn().createStatement();
		ResultSet rs = st
				.executeQuery("SELECT * FROM matches WHERE (dat between "
						+ date1 + " AND " + date2 + ") AND  compid = " + compId);
		listFill(rs);
		rs.close();
		st.close();
		conn.close();
	}

	public void getMatchesDateBefore(Date date) throws SQLException {
		getCoonection();
		Statement st = conn.getConn().createStatement();
		ResultSet rs = st.executeQuery("SELECT * FROM matches WHERE dat <= "
				+ date);
		listFill(rs);
		rs.close();
		st.close();
		conn.close();
	}

	public void getMatchesDateAfter(Date date) throws SQLException {
		getCoonection();
		Statement st = conn.getConn().createStatement();
		ResultSet rs = st.executeQuery("SELECT * FROM matches WHERE dat >= "
				+ date);
		listFill(rs);
		rs.close();
		st.close();
		conn.close();
	}

	public void listFill(ResultSet rs) throws SQLException {
		MatchObj mobj;
		while (rs.next()) {
			mobj = new MatchObj();
			mobj.setmId(rs.getLong(1));
			mobj.setComId(rs.getInt(2));
			mobj.setT1(rs.getString(3));
			mobj.setT2(rs.getString(4));
			mobj.setFt1(rs.getInt(5));
			mobj.setFt2(rs.getInt(6));
			mobj.setHt1(rs.getInt(7));
			mobj.setHt1(rs.getInt(8));
			mobj.set_1(rs.getFloat(9));
			mobj.set_x(rs.getFloat(10));
			mobj.set_2(rs.getFloat(11));
			mobj.set_o(rs.getFloat(12));
			mobj.set_u(rs.getFloat(13));
			mobj.setDat(rs.getDate(14));
			mlist.add(mobj);
		}
	}

}
