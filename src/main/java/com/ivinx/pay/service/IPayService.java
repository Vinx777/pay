package com.ivinx.pay.service;

import com.ivinx.pay.pojo.PayInfo;
import com.lly835.bestpay.enums.BestPayTypeEnum;
import com.lly835.bestpay.model.PayResponse;

import java.math.BigDecimal;

/**
 * Created by Vinx
 */
public interface IPayService {

	PayResponse create(String orderId, BigDecimal amount, BestPayTypeEnum bestPayTypeEnum);

	/**
	 * @param notifyData
	 */
	String asyncNotify(String notifyData);

	/**
	 * @param orderId
	 * @return
	 */
	PayInfo queryByOrderId(String orderId);
}
