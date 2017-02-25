package com.rishabh.github.instagrabber.database;

public class InstaImage {

    private int _id;
    private String _name;
    private String _instaImageURL;
    private String _phoneImageURL;
    private String _caption;

    public InstaImage() {
    }

    public InstaImage(int _id, String _name, String _instaImageURL, String _phoneImageURL,
        String _caption) {
        this._id = _id;
        this._name = _name;
        this._instaImageURL = _instaImageURL;
        this._phoneImageURL = _phoneImageURL;
        this._caption = _caption;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String get_name() {
        return _name;
    }

    public void set_name(String _name) {
        this._name = _name;
    }

    public String get_instaImageURL() {
        return _instaImageURL;
    }

    public void set_instaImageURL(String _instaImageURL) {
        this._instaImageURL = _instaImageURL;
    }

    public String get_phoneImageURL() {
        return _phoneImageURL;
    }

    public void set_phoneImageURL(String _phoneImageURL) {
        this._phoneImageURL = _phoneImageURL;
    }

    public String get_caption() {
        return _caption;
    }

    public void set_caption(String _caption) {
        this._caption = _caption;
    }
}
