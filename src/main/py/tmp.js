
var big_categories = {}

var all = document.querySelectorAll('.pj-rank-cate-item a');
for(var i = 0; i < all.length; i++) {
    var a = all[i];
    var id = a.attributes['data-id'].value;
    var show = a.text;
    big_categories[id] = show;
    console.log(id, show)
}

console.log(JSON.stringify(big_categories))