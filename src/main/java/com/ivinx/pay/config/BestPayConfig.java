package com.ivinx.pay.config;

import com.lly835.bestpay.config.AliPayConfig;
import com.lly835.bestpay.config.WxPayConfig;
import com.lly835.bestpay.service.BestPayService;
import com.lly835.bestpay.service.impl.BestPayServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * Created by Vinx
 */
@Component
public class BestPayConfig {

	@Autowired
	private WxAccountConfig wxAccountConfig;

	@Autowired
	private AlipayAccountConfig alipayAccountConfig;

	@Bean
	public BestPayService bestPayService(WxPayConfig wxPayConfig) {
		AliPayConfig aliPayConfig = new AliPayConfig();
		aliPayConfig.setAppId(alipayAccountConfig.getAppId());
		aliPayConfig.setPrivateKey(alipayAccountConfig.getPrivateKey());
		aliPayConfig.setAliPayPublicKey(alipayAccountConfig.getPublicKey());
		aliPayConfig.setNotifyUrl(alipayAccountConfig.getNotifyUrl());
		aliPayConfig.setReturnUrl(alipayAccountConfig.getReturnUrl());

		BestPayServiceImpl bestPayService = new BestPayServiceImpl();
		bestPayService.setWxPayConfig(wxPayConfig);
		bestPayService.setAliPayConfig(aliPayConfig);
		return bestPayService;
	}

	@Bean
	public WxPayConfig wxPayConfig() {
		WxPayConfig wxPayConfig = new WxPayConfig();
		wxPayConfig.setAppId(wxAccountConfig.getAppId());
		wxPayConfig.setMchId(wxAccountConfig.getMchId());
		wxPayConfig.setMchKey(wxAccountConfig.getMchKey());
		wxPayConfig.setNotifyUrl(wxAccountConfig.getNotifyUrl());
		wxPayConfig.setReturnUrl(wxAccountConfig.getReturnUrl());
		return wxPayConfig;
	}
}
