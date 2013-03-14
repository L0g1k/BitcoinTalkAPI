package dev.bitcoin.bitcointalk;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class BitcoinTalkScaperServletBase extends HttpServlet {

	protected final BitcoinTalkWAPScraper scraper = new BitcoinTalkWAPScraper();
	protected final Database database = new Database();
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	
	}

}
