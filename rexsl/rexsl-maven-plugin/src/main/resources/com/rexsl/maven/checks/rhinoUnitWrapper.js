/*
Copyright (c) 2008, Tiest Vilee
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
    * The names of its contributors may not be used to endorse or promote products derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

importClass(java.io.File);
importClass(java.io.FileReader);
importClass(org.apache.commons.io.IOUtils);

var options;
var testfailed = false;

function loadFile(fileName) {
	var file = new File(fileName);
	if (file.isAbsolute() === false)
	{
		file = new File(base, fileName);
	}
	var reader = new FileReader(file);
	return new String(IOUtils.toString(reader)).toString()
}

eval(rhinoUnitUtil);



function addTestsToTestObject(tests, TestObject) {
	var whenCases = 0;
	forEachElementOf(tests, function (parameter) {
		if (parameter instanceof Function) {
			TestObject[getFunctionNameFor(parameter)] = parameter;
		} else {
			TestObject["when" + whenCases] = parameter;
			whenCases += 1;
		}
	});
}

function testCases(testObject) {
	var args = AssertionException.cloneArray(arguments);
	args.shift(); // remove first element
	addTestsToTestObject(args, testObject);
}

function when(setUp) {
	var whenCase = {
		setUp: setUp
	};

	var args = AssertionException.cloneArray(arguments);
	args.shift(); // remove first element
	addTestsToTestObject(args, whenCase);

	return whenCase;
}

function runTest(file) {
    fails = [];

	function executeTest(tests, testName, superSetUp, superTearDown) {
		testCount++;
		try {
			if (superSetUp) {
				superSetUp();
			}
			tests[testName]();
			assert.test();
		} catch (e) {
            /*
			if (e.constructor === AssertionException) {
				fails.push(testName + ", " + e);
			} else {
                errors.push(testName + ", " + e);
			}*/
            fails.push(testName + ", " + e);
			testfailed = true;
		}
		if (superTearDown) {
			superTearDown();
		}
	}

	var before = {}, after = {};
	function wrapFunction$With$Position$(wrapped, wrapping, position) {
		if (wrapping) {
			return function () {
				if (position === before && wrapped) {
					wrapped();
				}
				wrapping();
				if (position === after && wrapped) {
					wrapped();
				}
			};
		}
		return wrapped;
	}

	function executeTestCases(tests, superSetUp, superTearDown) {

		for (var testName in tests) {
			assert = new Assert();

			if (testName === "setUp" || testName === "tearDown") {
				continue;
			}

			var theTest = tests[testName];

			if (theTest instanceof Function) {
				executeTest(tests, testName, superSetUp, superTearDown);
			} else {
				var newSuperSetup = wrapFunction$With$Position$(superSetUp, theTest.setUp, before);
				var newSuperTearDown = wrapFunction$With$Position$(superTearDown, theTest.tearDown, after);
				executeTestCases(theTest, newSuperSetup, newSuperTearDown);
			}
		}
	}

	var assert;
	var test = {};

	eval(loadFile(file));

	var testCount = 0;
	var failingTests = 0;
	var erroringTests = 0;

	executeTestCases(test, test.setUp, test.tearDown);

	if (testCount === 0) {
		erroringTests += 1;
		testfailed = true;
		fails.push(file + ": No tests defined");
	}

    function formatMessages() {
        res = "";
        for (var fail in fails) {
            res += fail + "\n";
        }
        return res;
    }

    return formatMessages();
}