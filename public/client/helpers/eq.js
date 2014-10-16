// equality, for use with handlebars if statements

module.exports = [
    'eq',
    function (v1, v2) {
        return v1 == v2;
    }
];
