package dev.bitcoin.bitcointalk.model.appengine;

import static com.googlecode.objectify.ObjectifyService.ofy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Transient;

import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Load;
import com.googlecode.objectify.annotation.OnSave;
import com.googlecode.objectify.annotation.Unindex;


@Entity
@Cache(expirationSeconds=60)
public class Topic implements HasFreshness {

	@Id transient Long id;
	public String title;
	@Index public String topicId;
	@Unindex Date dateLastUpdated;
	@Unindex public String parentBoardId;
	@Unindex boolean isBeingUpdated = true;
	@Transient public Collection<TopicPage> pages;
	@Unindex public int postCount;
	@Load transient List<Ref<TopicPage>> _pages;
	public String parentBoardName;
	@Transient
	public TopicPage requestedPage;
	@Transient int previousPageLink;
	@Transient int nextPageLink;
	@Transient int numberPages;
	boolean sticky;
	@Transient static int freshness = 60*1000; // 60 Seconds
	// 'Wizard of oz' pattern ;)
	@Transient private static transient int[] stickyTopics = {
		// General
		156942, 7269, 154516, 20333,  
		// Newbies
		15958, 33835, 15672, 15911, 86580, 
		128314, 126798, 17240, 20292,
		15918, 86580,
		// Mining
		123726, 9430, 16169,
		// Dev & Tech
		4571, 15527, 41718, 151,
		// Speculation
		131829, 130925,
		// Tech Support
		323
		//
	};
	public Topic() { }
	public Topic(String title, String topicId) {
		this.title = title;
		this.topicId = topicId;
	}
	
	public void loadPageCount() {
		numberPages = getPageCount(); 
	}
	public int getPageCount() {
		return _pages != null ? _pages.size() : 0;
	}

	@OnSave
	void saveDate() {
		dateLastUpdated = new Date();
	}
	
	@Override
	public Date getLastUpated() {
		return dateLastUpdated;
	}
	
	@Override
	public int getFreshnessTime() {
		return freshness;
	}
	
	/*public void setPosts(List<Post> posts) {
		ofy().save().entities(posts).now();
		List<Ref<Post>> boardRefs = new ArrayList<Ref<Post>>();
		for (Post post : posts) {
			boardRefs.add(Ref.create(post));
		}
		this._posts = boardRefs;
	}*/
	
	@Override
	public boolean isBeingUpdated() {
		return isBeingUpdated;
	}

	@Override
	public void setBeingUpdated(boolean updated) {
		this.isBeingUpdated = updated;
	}
	@Override
	public String getId() {
		return topicId;
	}
	
	public void save() {
		int id = 0;
		try {
			id = Integer.valueOf(topicId);
		} catch (Exception e) {
			e.printStackTrace();
			sticky = false;
		}
		for (int i = 0; i < stickyTopics.length; i++) {
			if(stickyTopics[i] == id) {
				sticky = true;
				break;
			}
		}
		ofy().save().entity(this).now();
	}
	
	public void loadPages() {
		pages = getPages();
		Iterator<TopicPage> iterator = pages.iterator();
		for (int i = 0; i < pages.size(); i++) {
			iterator.next().pageId = String.valueOf(i + 1);
		}
	}
	
	public void loadLastPage() {
		final int pageIndex = _pages.size();
		loadPageFromIndex(pageIndex);
	}
	/**
	 * 
	 * @param pageIndex 1 based
	 */
	public void loadPageFromIndex(final int pageIndex) {
		final Ref<TopicPage> pageKey = _pages.get(pageIndex-1);
		this.requestedPage = pageKey.get();
		if(pageIndex >= _pages.size()) {
			nextPageLink = -1;
		} else {
			nextPageLink = pageIndex + 1;
		}
		if(pageIndex == 1 || _pages.size() == 1) {
			previousPageLink = -1;
		} else {
			previousPageLink = pageIndex - 1;
		}
		requestedPage.loadPosts();
		
	}
	
	public Collection<TopicPage> getPages() {
		if(_pages == null)
			return new ArrayList<TopicPage>();
		
		return ofy().load().refs(_pages).values();
	}
	
	public void setPages(List<TopicPage> pages) {
		ofy().save().entities(pages).now();
		List<Ref<TopicPage>> topicPageRefs = new ArrayList<Ref<TopicPage>>();
		for (TopicPage topicPage : pages) {
			topicPageRefs.add(Ref.create(topicPage));
		}
		this._pages = topicPageRefs;
	}
	
	public boolean isUnripe() {
		return _pages == null || _pages.size() == 0;
	}
}
