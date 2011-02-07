/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package acs;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author roman
 */
class Phone {
    private ArrayList<Repo> repos = new ArrayList<Repo>();
    private String name;
    private String repoListURL;

    public Phone(String n, String url) {
        name = n;
        repoListURL = url;
    }

    public String toString() {
        return name;
    }

    public String getName() {
        return name;
    }

    public List<Repo> getRepos() {
        return repos;
    }

    public String getRepoListURL() {
        return repoListURL;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRepoListURL(String repoListURL) {
        this.repoListURL = repoListURL;
    }

}
