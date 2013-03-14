package dev.bitcoin.bitcointalk.model.appengine;

import static com.googlecode.objectify.ObjectifyService.ofy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.persistence.Transient;

import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Load;
import com.googlecode.objectify.annotation.OnSave;
import com.googlecode.objectify.annotation.Unindex;


@Entity
public class Topic implements HasFreshness {

	@Id transient Long id;
	public String title;
	@Index public String topicId;
	@Unindex Date dateLastUpdated;
	@Unindex public String parentBoardId;
	@Unindex private transient boolean isBeingUpdated;
	@Transient public Collection<TopicPage> pages;
	@Unindex public int postCount;
	@Load transient List<Ref<TopicPage>> _pages;
	//@Load transient List<Ref<Post>> _posts;
	//public Collection<Post> posts;
	public String parentBoardName;
	@Transient
	public TopicPage requestedPage;
	@Transient int previousPageLink;
	@Transient int nextPageLink;
	@Transient int numberPages;
	@Transient static int freshness = 60*1000; // 60 Seconds
	
	public Topic() {}
	public Topic(String title, String topicId) {
		this.title = title;
		this.topicId = topicId;
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
		ofy().save().entity(this).now();
	}
	
	public void loadPages() {
		pages = getPages();
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
		if(pageIndex >= _pages.size() - 1) {
			nextPageLink = -1;
		} else {
			nextPageLink = pageIndex + 1;
		}
		if(pageIndex == 0 || _pages.size() == 1) {
			previousPageLink = -1;
		} else {
			previousPageLink = pageIndex - 1;
		}
		requestedPage.loadPosts();
		numberPages = _pages.size();
	}
	
	private Collection<TopicPage> getPages() {
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
