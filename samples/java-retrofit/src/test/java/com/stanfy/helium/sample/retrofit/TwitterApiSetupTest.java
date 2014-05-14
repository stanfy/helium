package com.stanfy.helium.sample.retrofit;

import com.stanfy.helium.sample.retrofit.api.MyBackend;
import com.stanfy.helium.sample.retrofit.api.Post;
import com.stanfy.helium.sample.retrofit.api.SearchResponse;
import com.stanfy.helium.sample.retrofit.api.User;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

import static org.mockito.Mockito.*;

public class TwitterApiSetupTest {

  @Test
  public void setupTest() {
    assertNotNull(TwitterApiSetup.setup());
  }

  @Test
  public void itCanFailOnCompileStep() {
    MyBackend api = mock(MyBackend.class);
    //noinspection UnusedAssignment
    SearchResponse resp = api.searchPosts("test", 1);
    resp = new SearchResponse();
    resp.statuses = new ArrayList<Post>();
    Post post = new Post();
    post.user = new User();
    resp.statuses.add(post);
  }

}
