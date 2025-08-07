// Test JavaScript file for action link references
// This file tests the JavaScriptActionLinkReferenceProvider functionality

// Test with .do extension (traditional)
var url1 = '/prpins/policyImport/preEndorseChangePlans.do?certiNo=' + applyNo;

// Test with .action extension
var url2 = '/user/login.action';

// Test with other possible extensions
var url3 = '/admin/dashboard.htm';

// Test with namespace and action
var actionUrl = '/common/processCodeInputContinue.do';

// Test in function call
function redirectToAction() {
    window.location.href = '/policy/save.action?id=' + policyId;
}

// Test in object property
var config = {
    loginUrl: '/auth/login.do',
    logoutUrl: '/auth/logout.action'
};

// Test with complex URL
var complexUrl = '/module/submodule/complexAction.do?param1=value1&param2=value2';

// Test with concatenation
var dynamicUrl = '/base/' + moduleName + '/action.do';