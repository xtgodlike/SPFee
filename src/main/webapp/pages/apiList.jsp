<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta http-equiv="pragma" content="no-cache" /> 
<meta http-equiv="Cache-Control" content="no-cache, must-revalidate" /> 
<jsp:include page="base.jsp" />
<title>计费平台开发者中心</title>
</head>
<body>
	<jsp:include page="menu.jsp" />
	<div class="api_div_list">
		<table>
			<thead>
				<tr>
					<th>服务接口</th>
				</tr>
			</thead>
			
			<tbody>
				<tr style="height: 10px;">
					<td></td>
				</tr>
			</tbody>
			
			<tbody>
				<tr>
					<td>
						●<a href="<%=request.getContextPath()%>/api/paymentSms">&nbsp;&nbsp;getPaySms</a>【短信二次确认支付接口,2次请求】<span style="color: #13BA0D;">【推荐使用】</span>
					</td>
				</tr>
				
				<tr>
					<td>
						●<a href="<%=request.getContextPath()%>/api/paymentSmsTotal">&nbsp;&nbsp;getPaySmsTotal</a>【短信二次确认支付接口1次请求】<span style="color: #13BA0D;">【推荐使用】</span>
					</td>
				</tr>
				
				<tr>
					<td>
						●<a href="<%=request.getContextPath()%>/api/mzGetSms">&nbsp;&nbsp;getPaySmsForPc</a>【猫爪PC】<span style="color: #13BA0D;">【推荐使用】</span>
					</td>
				</tr>
				<tr>
					<td>
						●<a href="<%=request.getContextPath()%>/api/mzDdoGetSms">&nbsp;&nbsp;getDdoPaySms</a>【猫爪DDO】<span style="color: #13BA0D;">【推荐使用】</span>
					</td>
				</tr>
				
				<tr>
					<td>
						●<a href="<%=request.getContextPath()%>/api/cbGetAck">&nbsp;&nbsp;getCBAck</a>【创贝回调】<span style="color: #13BA0D;">【推荐使用】</span>
					</td>
				</tr>
				
				<tr>
					<td>
						●<a href="<%=request.getContextPath()%>/api/mzZhPaymentSms">&nbsp;&nbsp;getPaySmsZH</a>【联动优势综合支付】<span style="color: #13BA0D;">【推荐使用】</span>
					</td>
				</tr>
				
				<tr>
					<td>
						●<a href="<%=request.getContextPath()%>/api/ykGetPaymentSms">&nbsp;&nbsp;ykGetPaymentSms</a>【亿科统一支付】<span style="color: #13BA0D;">【推荐使用】</span>
					</td>
				</tr>				
				
				<tr>
					<td>
						●<a href="<%=request.getContextPath()%>/api/fyAck">&nbsp;&nbsp;fyAck</a>【昉寅同步回调接口】<span style="color: #13BA0D;">【推荐使用】</span>
					</td>
				</tr>
				
				<tr>
					<td>
						●<a href="<%=request.getContextPath()%>/api/qcAck">&nbsp;&nbsp;qcAck</a>【青城回调】<span style="color: #13BA0D;">【推荐使用】</span>
					</td>
				</tr>
				
				<tr>
					<td>
						●<a href="<%=request.getContextPath()%>/api/pzGetPaymentSms">&nbsp;&nbsp;pzGetPaymentSms</a>【平治】<span style="color: #13BA0D;">【推荐使用】</span>
					</td>
				</tr>
				
				<tr>
					<td> 
						●<a href="<%=request.getContextPath()%>/api/lxxGetSms">&nbsp;&nbsp;lxxGetSms</a>【蓝讯轩PC网游(刀剑)】<span style="color: #13BA0D;">【推荐使用】</span>
					</td>
				</tr>
				
				<tr>
					<td> 
						●<a href="<%=request.getContextPath()%>/api/lxxTTGetSms">&nbsp;&nbsp;lxxGetSms </a>【蓝讯轩PC网游(桶桶)】<span style="color: #13BA0D;">【推荐使用】</span>
					</td>
				</tr>					

				<tr>
					<td>
						●<a href="<%=request.getContextPath()%>/api/pzWoStoreGetPayment">&nbsp;&nbsp;pzWoStoreGetPayment</a>【平治沃商店】<span style="color: #13BA0D;">【推荐使用】</span>
					</td>
				</tr>
				
				<tr>
					<td>
						●<a href="<%=request.getContextPath()%>/api/xrGetSms">&nbsp;&nbsp;xrGetSms</a>【讯睿RDO短代】<span style="color: #13BA0D;">【推荐使用】</span>
					</td>
				</tr>
				
				<tr>
					<td>
						●<a href="<%=request.getContextPath()%>/api/qyRDOGetSms">&nbsp;&nbsp;qyRDOGetSms</a>【千雅RDO测试】<span style="color: #13BA0D;">【推荐使用】</span>
					</td>
				</tr>
				
				<tr>
					<td>
						●<a href="<%=request.getContextPath()%>/api/testSdk">&nbsp;&nbsp;testSdk</a>【SDK测试】
					</td>
				</tr>
				
				<tr>
					<td>
						●<a href="<%=request.getContextPath()%>/api/mgDdoGetSms">&nbsp;&nbsp;mgGetSms</a>【咪咕DDO】<span style="color: #13BA0D;">【推荐使用】</span>
					</td>
				</tr>						
			</tbody>
			
		</table>
	</div>
</body>
</html>