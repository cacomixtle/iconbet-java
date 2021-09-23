package com.iconbet.score.daofund;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import score.Address;
import score.ArrayDB;
import score.BranchDB;
import score.Context;
import score.DictDB;
import score.VarDB;
import score.annotation.EventLog;
import score.annotation.External;
import score.annotation.Payable;

public class DaoFund {
	
	public String TAG = "ICONbet DAOfund";
	
    private static final String ADMINS = "admins";
    private static final String WITHDRAW_COUNT = "withdraw_count";
    private static final String WITHDRAW_RECORD = "withdraw_record";
    private static final BigInteger X_6 = new BigInteger("1000000"); // 10 ** 6
    private static final BigInteger BATCH_SIZE = BigInteger.valueOf(100L);
    

	private final ArrayDB<Address> admins = Context.newArrayDB(ADMINS, Address.class);
	private final VarDB<BigInteger> withdraw_count = Context.newVarDB(WITHDRAW_COUNT, BigInteger.class);
	//private final DictDB<String,LinkedList> withdraw_record = Context.newDictDB(WITHDRAW_RECORD, LinkedList.class);
	private BranchDB<BigInteger, DictDB<String, String>> withdraw_record = Context.newBranchDB(WITHDRAW_RECORD, String.class);


	
	/***
	  def on_update(self) -> None:
	        super().on_update()
	        self.admins.put(self.owner)
	        self.withdraw_count.set(0)
	 * @param <T>
	  ***/
	
	
	private <T> boolean remove_array_item(T value, ArrayDB<T> arraydb ) {
		
		return Boolean.FALSE;
		
	}
		
	
	/***
	
	def remove_array_item(array_db, target) -> bool:
	    _out = array_db[-1]
	    if _out == target:
	        array_db.pop()
	        return True
	    for index in range(len(array_db) - 1):
	        if array_db[index] == target:
	            array_db[index] = _out
	            array_db.pop()
	            return True
	    return False
***/

	@External(readonly = true)
	public String name() {
		/***
		:return: name of the Score
		***/
		
		return TAG;
	}
	
	@External
	public void add_admin(Address _admin) {
		Address sender = Context.getCaller();
		Address owner = Context.getOrigin();
		if (!sender.equals(owner)) {
			Context.revert(TAG + ": Only admins can set new admins.");
		}
		
		if (!containsInArrayDb(_admin, this.admins)) {
			this.admins.add(_admin);
			AdminAdded(_admin);
			
		}else {
			Context.revert(TAG + ":  "+ _admin +" is already on admin list.");
		}
	}
	
	@External
	public void remove_admin(Address _admin) {
		Address sender = Context.getCaller();
		Address owner = Context.getOrigin();
		if (!sender.equals(owner)) {
			Context.revert(TAG + ": Only admins can remove admins.");
		}
		
		if (_admin.equals(owner)) {
			Context.revert(TAG + ": Owner address cannot be removed from the admins list.");
		}
		
		if (containsInArrayDb(_admin, this.admins)) {
			remove_array_item(_admin, this.admins);
			AdminRemoved(_admin);
		}else {
			Context.revert(TAG + ":  "+ _admin +" not in Admins List");
		}
	}
	
	@External(readonly = true)
	public List<Address> get_admins() {
		
		List<Address> addressList = new ArrayList<>();

		for (int i=0; i< this.admins.size(); i++) {
			addressList.add(this.admins.get(i));
		}
		return addressList;
	}
	
	/***
	 * Add fund to the daoFund wallet
	 ***/
	@External
	@Payable
	public void add_fund() {
		
	}
		 
	@External
	public void withdraw_fund( Address _address, BigInteger _amount, String _memo) {
		Address sender = Context.getCaller();
		
		if (!containsInArrayDb(sender, this.admins)) {
			Context.revert(TAG + ": Only admins can run this method.");
		}

		BigInteger _available_amount = Context.getBalance(Context.getAddress());
		if ( _available_amount.compareTo(_amount) == -1 ) {
			Context.revert(TAG + ": Not Enough balance. Available Balance =" + _available_amount.toString());
		}
		
		try {
			
			BigInteger _count = this.withdraw_count.get();
			BigInteger _withdraw_count = _count.add(BigInteger.ONE);
			
			BigInteger now = BigInteger.valueOf(Context.getBlockTimestamp());		
			BigInteger day = BigInteger.ZERO;
			day = day.add(now.divide(X_6));
			
			this.withdraw_count.set(_withdraw_count);
			this.withdraw_record.at(_withdraw_count).set("withdraw_amount", _amount.toString());
			this.withdraw_record.at(_withdraw_count).set("withdraw_address", _address.toString());
			this.withdraw_record.at(_withdraw_count).set("withdraw_memo", _memo);
			this.withdraw_record.at(_withdraw_count).set("withdraw_timestamp", day.toString());
			
			// self.icx.transfer(_address, _amount)
			Context.transfer(_address, _amount);
			FundTransferred(_address, _amount.toString() + " transferred to " +_address.toString() + " for " + _memo);
		}catch(Exception e) {
			Context.revert(TAG + ": Network problem. Claiming Reward. Reason: " + e.getCause());
		}
	}
	
	@External(readonly = true)
	public BigInteger get_withdraw_count() {
		return this.withdraw_count.get();
	}
	
	@External(readonly = true)
	public 	ArrayDB<String> get_withdraw_records(BigInteger _start, BigInteger _end) {
		ArrayDB<String> withdrawRecord = Context.newArrayDB("withdrawRecord", String.class);
		BigInteger wd_count = this.withdraw_count.get();
		
		if ( !(_start != null || _end!= null || wd_count !=null) ) {
			withdrawRecord.add("No Records Found.");
			return withdrawRecord;
		}
		
		if (wd_count.compareTo(BigInteger.ZERO) == 0) {
			withdrawRecord.add("No Records Found.");
			return withdrawRecord;
		}
		
		if (_start.compareTo(BigInteger.ZERO) == 0 && _end.compareTo(BigInteger.ZERO)== 0 ) {
			_end = wd_count;
			Long max =  Math.max(1L, _end.longValue() - BATCH_SIZE.longValue());
			_start = BigInteger.valueOf(max);
		}else if ( _end.compareTo(BigInteger.ZERO)== 0 ) {
			Long min =  Math.min(wd_count.longValue(), _start.longValue() + BATCH_SIZE.longValue());
			_end = BigInteger.valueOf( min);
		}else if (_start.compareTo(BigInteger.ZERO) == 0 ) {
			Long max =  Math.max(1L, _end.longValue() - BATCH_SIZE.longValue());
			_start = BigInteger.valueOf(max);
		}
		
		if (_end.compareTo(wd_count) == 1) {
			  _end = wd_count;
		}
		
		if ( _start.compareTo(_end) >= 1) {
			withdrawRecord.add("Start must not be greater than or equal to end.");
			return withdrawRecord;
		}
		
		if ( _end.subtract(_start).compareTo(BATCH_SIZE) == 1 ) {
			withdrawRecord.add("Maximum allowed range is " +BATCH_SIZE.longValue());

			return withdrawRecord;
		}
		
		for( int _withdraw = _start.intValue();  _withdraw<=_end.intValue(); _withdraw++) {
			
			BigInteger idx = BigInteger.valueOf(_withdraw);
			
			String withdraw_address 	=  this.withdraw_record.at(idx).get("withdraw_address");		
			String withdraw_timestamp 	=  this.withdraw_record.at(idx).get("withdraw_timestamp");
			String withdraw_reason 		=  this.withdraw_record.at(idx).get("withdraw_reason");
			String withdraw_amount 		=  this.withdraw_record.at(idx).get("withdraw_amount");
			
			withdrawRecord.add(withdraw_address);
			withdrawRecord.add(withdraw_timestamp);
			withdrawRecord.add(withdraw_reason);
			withdrawRecord.add(withdraw_amount);
			
		}
	
		
		return withdrawRecord;
	}
	
	
	@External(readonly = true)
	public  ArrayDB<String> get_withdraw_record_by_index(BigInteger _idx ) {
		ArrayDB<String> withdrawRecord = Context.newArrayDB("withdrawRecord", String.class);	 
		BigInteger _count = this.withdraw_count.get();
		
		if (_idx.compareTo(BigInteger.ZERO) <= 0 || _idx.compareTo(_count) == 1 ) {
			withdrawRecord.set(-1,  _idx.toString() +" must be in range [1," +_count.toString() + "]");
			return withdrawRecord;
		}
		
		String withdraw_address 	=  this.withdraw_record.at(_idx).get("withdraw_address");		
		String withdraw_timestamp 	=  this.withdraw_record.at(_idx).get("withdraw_timestamp");
		String withdraw_reason 		=  this.withdraw_record.at(_idx).get("withdraw_reason");
		String withdraw_amount 		=  this.withdraw_record.at(_idx).get("withdraw_amount");

		withdrawRecord.add(withdraw_address);
		withdrawRecord.add(withdraw_timestamp);
		withdrawRecord.add(withdraw_reason);
		withdrawRecord.add(withdraw_amount);
		
		return withdrawRecord;
	}
	
	@Payable
	public void fallback() {}
	
	@EventLog(indexed=1)
	public void AdminAdded(Address _address) {}	
	        
	@EventLog(indexed=1)
	public void AdminRemoved(Address _address) {}	
        
	@EventLog(indexed=1)
	public void FundTransferred(Address _address, String note) {}	
        
	
/***	
	private LinkedList<String> getWithdrawRecord(String property){
		LinkedList<String> withdrawRecord = this.withdraw_record.get(property);
		if(withdrawRecord == null) {
			withdrawRecord = new LinkedList<String>();
			this.withdraw_record.set(property, withdrawRecord);
		}
		return this.withdraw_record.get(property);
	}
	
	
	private LinkedList<String> getWithdrawRecordReadOnly(String property){
		LinkedList<String> withdrawRecord = this.withdraw_record.get(property);
		if(withdrawRecord == null) {
			withdrawRecord = new LinkedList<String>();
		}
		return withdrawRecord;
	}
	
***/	
	private <T> Boolean containsInArrayDb(T value, ArrayDB<T> arraydb) {
		boolean found = false;
		if(arraydb == null || value == null) {
			return found;
		}

		for(int i = 0; i< arraydb.size(); i++) {
			if(arraydb.get(i) != null
					&& arraydb.get(i).equals(value)) {
				found = true;
				break;
			}
		}
		return found;
	}
	
	
}
