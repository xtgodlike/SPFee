<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta http-equiv="pragma" content="no-cache" /> 
<meta http-equiv="Cache-Control" content="no-cache, must-revalidate" /> 
<jsp:include page="base.jsp" />
<title>计费平台开发者中心-计费能力2.0版</title>
</head>
<body>
	<jsp:include page="menu.jsp" />
	<div class="api_nav_title">
		 单击<a href="<%=request.getContextPath()%>/api/apiList">此处</a>，获取完整的操作列表。
	</div>
	
	<div class="nav_left">
		<a href="<%=request.getContextPath()%>/api/paymentSms">获取支付短信接口</a>
		<a href="<%=request.getContextPath()%>/api/paymentConfirm">获取支付验证码</a>
		<a href="<%=request.getContextPath()%>/api/codeList"  id="current_nav">结果码</a>
	</div>
	
	<div class="api_from_div">
		
		<table class="tab1">
			<thead>
				<tr>
					<th colspan="2">结果码</th>
				</tr>
			</thead>
		</table>
		
		<div class="tab_title">短信二次确认支付接口结果码</div>
		<table class="tab3" cellpadding="0" border="0" cellspacing="0" style="margin-bottom: 20px;" >
			<thead>
				<tr>
					<th align="left" width="10%">结果码</th>
					<th align="left">说明</th>
				</tr>
			</thead>
			<jsp:include page="code.jsp" />
		</table>
	</div>
</body>
</html>