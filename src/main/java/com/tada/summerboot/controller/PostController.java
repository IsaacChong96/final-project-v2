package com.tada.summerboot.controller;

import org.springframework.ui.Model;
import com.tada.summerboot.model.Post;
import com.tada.summerboot.model.User;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.tada.summerboot.service.PostServiceImpl;
import com.tada.summerboot.service.UserServiceImpl;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;

@Controller
public class PostController {

    @Autowired
    PostServiceImpl post_service_implementation;

    @Autowired
    UserServiceImpl user_service_implementation;

    @GetMapping(path="/post/show-username/{id}", produces = { MediaType.APPLICATION_JSON_VALUE })
    public String showUserName(Model model, @PathVariable("id") Integer id) {
        Optional <Post> post = post_service_implementation.getPost(id);
        model.addAttribute("post", post.get());
        Optional <User> user = user_service_implementation.getUser( post.get().getUser_id());
        model.addAttribute("user", user.get());
        //$(user.username)?
        return "show-post";
    }

    @GetMapping(value="post-image") // it will be set to be /product
    public String postWithImage(Model model){
        User user = user_service_implementation.current_user();
        model.addAttribute("user", user);
        model.addAttribute("post", new Post());
        return "publish";
    }

    @GetMapping(value="/")
    public String everypostWithoutTable(Model model){
        List<Post> list = post_service_implementation.getAllPosts(); // get all the posts
        List<User> userList = new ArrayList<>(); // prepare an empty list

        for (int i = 0; i < list.size(); i++) {
            Post post = list.get(i);
            Optional<User> user = user_service_implementation.getUser(post.getUser_id()); // get the user object
            userList.add(user.get()); // add it to the userList
        }

        model.addAttribute("posts", list);
        model.addAttribute("users", userList); // make it available via users variable. Use stats.index to access specific index.

        return "index";
    }

    @GetMapping(value="/every-posts-by-single-user")
    public String everypostByIndividual(Model model){
        User user = user_service_implementation.current_user();
        List<Post> list = post_service_implementation.findAllByUserId(user.getId());
        model.addAttribute("posts", list);
        model.addAttribute("user", user);
        return "profile";
    }

    @GetMapping(value="/every-posts") // it will be set to be /product
    public String everyposts(Model model){
        List<Post> posts = post_service_implementation.getAllPosts();
        model.addAttribute("posts", posts); // this will pass the value to a ${user}
        return "examples/every-posts";
    }

    @GetMapping(value="/post") // it will be set to be /product
    public String post(Model model){
        User user = user_service_implementation.current_user();

        model.addAttribute("user", user);
        model.addAttribute("post", new Post());
        return "examples/post";
    }

    @PostMapping(path="post/image/new")
    public String newPostWithImage(@RequestParam(name="id", required = false) Integer id,
                                     @RequestParam(name="title", required = false) String title,
                                      @RequestParam(name="content", required = false) String content,
                                      @RequestParam(name="user_id", required = false) Integer user_id,
                                      @RequestParam(name="image") MultipartFile multipartFile) throws IOException {

        Post new_post = new Post(id, title, content, user_id, multipartFile.getBytes(), new Timestamp(Calendar.getInstance().getTime().getTime()));
        post_service_implementation.createOrUpdatePost(new_post);
        return "profile";
    }

    @GetMapping(path="/post/all", produces = { MediaType.APPLICATION_JSON_VALUE })
    public @ResponseBody
    List<Post> all() {
        return post_service_implementation.getAllPosts();
    }

    // this is for form-data
//    @PostMapping(path="/post/new")
//    public String newPost(@RequestParam(name="id", required=false) Integer id,
//                          @RequestParam(name="title", required = false) String title,
//                          @RequestParam(name="content", required = false) String content,
//                          @RequestParam(name="user_id", required = false) Integer user_id,
//                          @RequestParam(name="imageURL", required = false) String imageURL) {
//
//        post_service_implementation.createOrUpdatePost(new Post(id, title, content, user_id, imageURL));
//        return "examples/every-posts";
//    }

    //this is for javascript
    @PostMapping(path="/post/json/new", produces = { MediaType.APPLICATION_JSON_VALUE })
    public @ResponseBody String newPost(@RequestBody Post post) {
        //The Post post assumes that it has title, content, imageURL
        post_service_implementation.createPost(post);
        return "{\"status\": \"success\"}";
    }

    @GetMapping(path="/post/show/{id}", produces = { MediaType.APPLICATION_JSON_VALUE })
    public String show(Model model, @PathVariable("id") Integer id) {
        Optional <Post> post = post_service_implementation.getPost(id);
        model.addAttribute("post", post);
        return "examples/show-post";
    }

    //TODO - change to delete
    @GetMapping(path="/post/delete/{id}")
    public String destroy(@PathVariable("id") Integer id) {
        post_service_implementation.deletePost(id);
        return "index";
    }

    @RequestMapping(path = {"post/edit", "post/edit/{id}"})
    public String editPost(Model model, @PathVariable("id") Integer id)
    {
        if (id != null) { // when id is null, because it is not in the database
            User user = user_service_implementation.current_user();
            model.addAttribute("user", user);

            System.out.println("The id is not null");
            Optional<Post> entity = post_service_implementation.getPost(id);
            model.addAttribute("post", entity.get());
        } else { //else id is present, then we will just create a new entry in the database
            System.out.println("The id is NULL");
            model.addAttribute("post", new Post());
        }
        return "publish";
    }
}

//line 88 edited
