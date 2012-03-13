eval(loadFile("src/main/webapp/js/script.js"));
testCases(test,
function setUp() {
},

function shouldMultiply() {
    assert.that(f(3, 7), eq(15));
}
);
