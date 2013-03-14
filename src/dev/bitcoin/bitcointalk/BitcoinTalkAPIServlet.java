package dev.bitcoin.bitcointalk;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;

import dev.bitcoin.bitcointalk.model.appengine.Board;
import dev.bitcoin.bitcointalk.model.appengine.Category;
import dev.bitcoin.bitcointalk.model.appengine.Topic;

@Path("/v1")
@SuppressWarnings("serial")
public class BitcoinTalkAPIServlet extends HttpServlet {
	
	Map<String, String> responseCache = new HashMap<String, String>();
	Database database = new Database();
	
	@GET
	@Produces("application/json")
	public String getCategories(@Context HttpServletResponse servletResponse) throws MalformedURLException, IOException {
		setHeaders(servletResponse);
		final List<Category> categories = database.getCategories();
		for (Category category : categories) {
			category.boards = category.getBoards();
		}
		return new Gson().toJson(categories);
	}
	
	@GET
	@Produces("application/json")
	@Path("/boards/{boardId}")
	public Response getBoard(@Context HttpServletResponse servletResponse, @PathParam("boardId") String boardId) {
		setHeaders(servletResponse);
		final Board board = database.getBoard(boardId, true);
		if(board != null) {
			if(board.isUnripe()) {
				return Response.status(202).entity("The board was found, but it's topics haven't yet been downloaded. Please try again in 2-5 minutes").build();
			}
			board.loadTopics();
			return Response.ok(new Gson().toJson(board)).build();
		} else {
			return Response.status(404).build();
		}
	}
	
	@GET
	@Produces("application/json")
	@Path("/topics/{topicId}")
	public Response getTopic(
			@Context HttpServletResponse servletResponse, 
			@PathParam("topicId") String topicId,
			@QueryParam("pageId") String pageId) throws JsonIOException, IOException {
		setHeaders(servletResponse);
		final Topic topic = database.getTopic(topicId, true);
		
		if(topic != null) {
			if(topic.isUnripe()) {
				return Response.status(202).entity("The topic was found, but it's posts haven't yet been downloaded. Please try again in 2-5 minutes").build();
			} else {
				 if (pageId != null) {
					if(pageId.equals("latest")) 
						topic.loadLastPage();
					else
						tryToLoadPage(pageId, topic);
				}
				return Response.ok(new Gson().toJson(topic)).build();
			}
		} else {
			return Response.status(404).build();
		}
	}

	private void tryToLoadPage(String pageId, final Topic topic) {
		try {
			final int pageIdInt = Integer.parseInt(pageId);
			topic.loadPageFromIndex(pageIdInt);
		} catch (Exception e) {
			// Just don't load any page..
		}
	}
	
	@GET
	@Produces("application/json")
	@Path("/topics/{topicId}/pages/{pageId}")
	public Response getPage(
			@Context HttpServletResponse servletResponse, 
			@PathParam("topicId") String topicId,
			@PathParam("pageId") String pageId) throws JsonIOException, IOException {
		setHeaders(servletResponse);
		
		final Topic topic = database.getTopic(topicId, true);
		if(topic != null) {
			topic.loadPageFromIndex(Integer.parseInt(pageId));
			
			return Response.ok(new Gson().toJson(topic.requestedPage)).build();
		} else {
			return Response.status(404).build();
		}
	}
	
	void setHeaders(HttpServletResponse servletResponse) {
		servletResponse.addHeader("Access-Control-Allow-Origin", "*"); 
	}
}
