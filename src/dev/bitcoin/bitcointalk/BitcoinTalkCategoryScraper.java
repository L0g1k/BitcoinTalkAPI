package dev.bitcoin.bitcointalk;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.googlecode.objectify.cmd.LoadType;

import dev.bitcoin.bitcointalk.model.CategoryBoard;
import dev.bitcoin.bitcointalk.model.appengine.Board;
import dev.bitcoin.bitcointalk.model.appengine.Category;
import dev.bitcoin.bitcointalk.model.appengine.Post;
import dev.bitcoin.bitcointalk.model.appengine.Topic;
import dev.bitcoin.bitcointalk.model.appengine.TopicPage;
import static com.googlecode.objectify.ObjectifyService.ofy;


/**
 * Scrapes a Bitcointalk.org, and finds Categories/Boards/Child boards. This should seldom change.
 * 
 * @author Jason
 *
 */
public class BitcoinTalkCategoryScraper extends BitcoinTalkScaperServletBase {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		if(req.getParameter("delete") != null) {
			deleteAll(Post.class);
			deleteAll(TopicPage.class);
			deleteAll(Category.class);
			deleteAll(Topic.class);
			deleteAll(Board.class);
			return;
		} 
		List<Category> appengineCategories = new ArrayList<Category>();
		final List<dev.bitcoin.bitcointalk.model.Category> categories = scraper.getCategories(false);
		
		for (dev.bitcoin.bitcointalk.model.Category category : categories) 
			convertCategory(appengineCategories, category);
		
		database.saveCategories(appengineCategories);
	}

	private void deleteAll(Class<?> class1) {
		ofy().delete().keys(ofy().load().type(class1).keys()).now();
	}

	protected void convertCategory(List<Category> appengineCategories, dev.bitcoin.bitcointalk.model.Category category) {
		final Category appengineCategory = new Category(category.title);
		final List<CategoryBoard> boards = category.getBoards();
		final List<Board> appEngineBoards = new ArrayList<Board>();
		for (CategoryBoard categoryBoard : boards) {
			final Board oldBoard = database.getBoard( categoryBoard.boardId, false);
			appEngineBoards.add(oldBoard == null ? new Board(categoryBoard.boardName, categoryBoard.boardId) : oldBoard);
		}
		appengineCategory.setBoards(appEngineBoards);
		appengineCategories.add(appengineCategory);
	}
}
