AppController = MVC.Controller.extend('main', {
    load:function (params) {
        document.body.innerHTML += "<h1 id='hello'>Hello World</h1>";
    }
});