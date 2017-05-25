package com.qy.sp.fee.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "/api")
public class ApiController {

	@RequestMapping(value = "/apiList")
	public String apiList() {

		return "/apiList";
	}

	@RequestMapping(value = "/paymentSms")
	public String paymentSms() {

		return "/paymentSms";
	}
	
	@RequestMapping(value = "/paymentSmsTotal")
	public String paymentSmsTotal() {

		return "/paymentSmsTotal";
	}

	@RequestMapping(value = "/codeList")
	public String codeList() {

		return "/codeList";
	}

	@RequestMapping(value= "/paymentConfirm")
	public String paymentConfirm(){
		
		return "/paymentConfirm";
	}
	
	@RequestMapping(value= "/mzGetSms")
	public String mzGetSms(){
		
		return "/mzGetSms";
	}
	
	@RequestMapping(value= "/mzSmsPay")
	public String mzSmsPay(){
		
		return "/mzSmsPay";
	}
	
	@RequestMapping(value= "/mzDdoGetSms")
	public String mzDdoGetSms(){
		
		return "/mzDdoGetSms";
	}
	
	@RequestMapping(value= "/mzDdoSmsPay")
	public String mzDdoSmsPay(){
		
		return "/mzDdoSmsPay";
	}
	
	@RequestMapping(value= "/cbGetAck")
	public String cbGetAck(){
		
		return "/cbGetAck";
	}
	
	@RequestMapping(value= "/mzZhPaymentSms")
	public String mzZhPaymentSms(){
		
		return "/mzZhPaymentSms";
	}
	
	@RequestMapping(value= "/mzZhPayment")
	public String mzZhPayment(){
		
		return "/mzZhPayment";
	}
	
	@RequestMapping(value= "/ykGetPaymentSms")
	public String ykGetPaymentSms(){
		return "ykGetPaymentSms";
	}
	
	@RequestMapping(value= "/ykAck")
	public String ykAck(){
		return "ykAck";
	}
		
	@RequestMapping(value= "/qcAck")
	public String qcAck(){
		return "qcAck";
	}
	
	@RequestMapping(value= "/pzGetPaymentSms")
	public String pzGetPaymentSms(){
		return "pzGetPaymentSms";
	}
	
	@RequestMapping(value= "/pzConfirmPayment")
	public String pzConfirmPayment(){
		return "pzConfirmPayment";
	}
	
	@RequestMapping(value= "/lxxGetSms")
	public String lxxGetSms(){
		
		return "/lxxGetSms";
	}
	
	@RequestMapping(value= "/lxxSmsPay")
	public String lxxSmsPay(){
		
		return "/lxxSmsPay";
	}
	
	@RequestMapping(value= "/lxxTTGetSms")
	public String lxxTTGetSms(){
		
		return "/lxxTTGetSms";
	}
	
	@RequestMapping(value= "/lxxTTSmsPay")
	public String lxxTTSmsPay(){
		
		return "/lxxTTSmsPay";
	}
	
	@RequestMapping(value="/pzWoStoreGetPayment")
	public String pzWoStoreGetPayment(){
		return "pzWoStoreGetPayment";
	}
	
	@RequestMapping(value= "/xrGetSms")
	public String xrGetSms(){
		
		return "/xrGetSms";
	}
	
	@RequestMapping(value= "/testSdk")
	public String testSdk(){
		return "/testSdk";
	}
	@RequestMapping(value= "/pzWoStoreAckTest")
	public String pzWoStoreAckTest(){
		return "/pzWoStoreAckTest";
	}
	@RequestMapping(value= "/mgDdoGetSms")
	public String mgDdoGetSms(){
		
		return "/mgDdoGetSms";
	}
	
	@RequestMapping(value= "/mgDdoSmsPay")
	public String mgDdoSmsPay(){
		
		return "/mgDdoSmsPay";
	}
	
	@RequestMapping(value= "/qyRDOGetSms")
	public String qyRDOGetSms(){
		
		return "/qyRDOGetSms";
	}
	
	@RequestMapping(value= "/qyRDOSmsPay")
	public String qyRDOSmsPay(){
		
		return "/qyRDOSmsPay";
	}
}
