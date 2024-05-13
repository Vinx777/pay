package com.ivinx.pay.service.impl;

import com.google.gson.Gson;
import com.ivinx.pay.dao.PayInfoMapper;
import com.ivinx.pay.enums.PayPlatformEnum;
import com.ivinx.pay.pojo.PayInfo;
import com.ivinx.pay.service.IPayService;
import com.lly835.bestpay.enums.BestPayPlatformEnum;
import com.lly835.bestpay.enums.BestPayTypeEnum;
import com.lly835.bestpay.enums.OrderStatusEnum;
import com.lly835.bestpay.model.PayRequest;
import com.lly835.bestpay.model.PayResponse;
import com.lly835.bestpay.service.BestPayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Created by Vinx
 */
@Slf4j
@Service
public class PayServiceImpl implements IPayService {

	private final static String QUEUE_PAY_NOTIFY = "payNotify";

	@Autowired
	private BestPayService bestPayService;

	@Autowired
	private PayInfoMapper payInfoMapper;

	@Autowired
	private AmqpTemplate amqpTemplate;

	/**
	 * @param orderId
	 * @param amount
	 */
	@Override
	public PayResponse create(String orderId, BigDecimal amount, BestPayTypeEnum bestPayTypeEnum) {
		PayInfo payInfo = new PayInfo(Long.parseLong(orderId),
				PayPlatformEnum.getByBestPayTypeEnum(bestPayTypeEnum).getCode(),
				OrderStatusEnum.NOTPAY.name(),
				amount);
		payInfoMapper.insertSelective(payInfo);

		PayRequest request = new PayRequest();
		request.setOrderName("4559066-sdk");
		request.setOrderId(orderId);
		request.setOrderAmount(amount.doubleValue());
		request.setPayTypeEnum(bestPayTypeEnum);

		PayResponse response = bestPayService.pay(request);
		log.info("Initiate payment response={}", response);

		return response;

	}

	/**
	 * @param notifyData
	 */
	@Override
	public String asyncNotify(String notifyData) {
		PayResponse payResponse = bestPayService.asyncNotify(notifyData);
		log.info("Asynchronous notification response={}", payResponse);

		PayInfo payInfo = payInfoMapper.selectByOrderNo(Long.parseLong(payResponse.getOrderId()));
		if (payInfo == null) {
			throw new RuntimeException("The result queried through orderNo is null");
		}
		if (!payInfo.getPlatformStatus().equals(OrderStatusEnum.SUCCESS.name())) {
			if (payInfo.getPayAmount().compareTo(BigDecimal.valueOf(payResponse.getOrderAmount())) != 0) {
				throw new RuntimeException("The amount in the asynchronous notification is inconsistent with the amount in the database.orderNo=" + payResponse.getOrderId());
			}

			payInfo.setPlatformStatus(OrderStatusEnum.SUCCESS.name());
			payInfo.setPlatformNumber(payResponse.getOutTradeNo());
			payInfoMapper.updateByPrimaryKeySelective(payInfo);
		}

		amqpTemplate.convertAndSend(QUEUE_PAY_NOTIFY, new Gson().toJson(payInfo));

		if (payResponse.getPayPlatformEnum() == BestPayPlatformEnum.WX) {
			return "<xml>\n" +
					"  <return_code><![CDATA[SUCCESS]]></return_code>\n" +
					"  <return_msg><![CDATA[OK]]></return_msg>\n" +
					"</xml>";
		}else if (payResponse.getPayPlatformEnum() == BestPayPlatformEnum.ALIPAY) {
			return "success";
		}

		throw new RuntimeException("Wrong payment platform in async notification");
	}

	@Override
	public PayInfo queryByOrderId(String orderId) {
		return payInfoMapper.selectByOrderNo(Long.parseLong(orderId));
	}
}
