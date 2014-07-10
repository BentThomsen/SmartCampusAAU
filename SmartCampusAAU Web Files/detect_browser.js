function IsMobileOs(userAgentString) {
	if (navigator.userAgent.toUpperCase().indexOf(userAgentString) > -1)
		return true;
	else
		return false;
}

function IsApple() {
	//Sub-agents: iphone, ipod, ipad
	return IsMobileOs("APPLE");
}

function IsWindows() {
	return IsMobileOs("WINDOWS");
}

function IsAndroid() {
	return IsMobileOs("ANDROID");
}