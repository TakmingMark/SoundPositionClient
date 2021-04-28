package com.example.mark.voiceposition;

/**
 * order to calculate complex number of chirp sound
 */
class ComplexNumber
{
	//define real part and imaginary part of complex number
	private double RealPart , ImaginPart;
	//no parameters construction
	public ComplexNumber(){
		RealPart = 0;
		ImaginPart = 0;
	}
	//have parameters construction
	public ComplexNumber(double r,double i){
		RealPart = r;
		ImaginPart = i;
	}
	// get the current real part of object
	public  double getRealPart(){
		return RealPart;
	}
	//get the current imaginary part of object
	public double getImaginPart(){
		return ImaginPart;
	}
	//modify the current real part of object
	public void setRealPart(double r){
		RealPart = r;
	}
	//modify the current imaginary part of object
	public void setImaginPart(double i){
		ImaginPart = i;
	}
	//the method is  current complex number plus parameter of complex number of object
	public ComplexNumber ComplexNumberAdd(ComplexNumber c){
		/*
		because real part and imaginary part is private define,
		so must use setRealPart() and setImaginPart() modify number
		*/
		ComplexNumber result = new ComplexNumber();
		// real part of complex number plus real part of complex number
		result.setRealPart(this.RealPart+c.getRealPart());
		// imaginary part of complex number plus imaginary part of complex number
		result.setImaginPart(this.ImaginPart+c.getImaginPart());
		return result;
	}
	//当前复数对象与一实数相加;
	public ComplexNumber ComplexNumberAdd(double c){
		ComplexNumber result = new ComplexNumber();
		//实部加上实数;
		result.setRealPart(this.RealPart+c);
		//虚部不变;
		result.setImaginPart(this.ImaginPart);
		return result;
	}
	//当前复数对象减去形参复数对象;
	public ComplexNumber ComplexNumberMinus(ComplexNumber c){
		ComplexNumber result = new ComplexNumber();
		//当前复数的实部减去形参对象的虚部;
		result.setRealPart(this.RealPart-c.getRealPart());
		//当前复数的虚部减去形参对象的虚部;
		result.setImaginPart(this.ImaginPart-c.getImaginPart());
		return result;
	}
	//当前复数对象减去某一实数;
	public ComplexNumber ComplexNumberMinus(double c){
		ComplexNumber result = new ComplexNumber();
		//复数对象的实部减去实数;
		result.setRealPart(this.RealPart-c);
		//复数对象的虚部不变;
		result.setImaginPart(this.ImaginPart);
		return result;
	}
	//当前复数对象乘以形参复数对象运算的方法;
	public ComplexNumber ComplexNumberMuti(ComplexNumber c){
		ComplexNumber result = new ComplexNumber();
		//当前复数与形参复数相乘后实部的结果;
		result.setRealPart(this.RealPart*c.getRealPart()-this.ImaginPart*c.getImaginPart());
		//当前复数与形参复数相乘后虚部的结果;
		result.setImaginPart(this.RealPart*c.getImaginPart()+this.ImaginPart*c.getRealPart());
		return result;
	}
	//当前复数与某一实数相乘的运算方法;
	public ComplexNumber ComplexNumberMuti(double c){
		ComplexNumber result = new ComplexNumber();
		result.setRealPart(this.RealPart*c);
		result.setImaginPart(this.ImaginPart*c);
		return result;
	}
	//当前复数对象除以某一复数的运算方法;
	public ComplexNumber ComplexNumberDivi(ComplexNumber c){
		ComplexNumber result = new ComplexNumber();
		//复数c中实部和虚部的平方和；
		double d = c.getRealPart()*c.getRealPart()+c.getImaginPart()*c.getImaginPart();
		result.setRealPart((this.RealPart*c.getRealPart()+this.ImaginPart*c.getImaginPart())/d);
		result.setImaginPart((this.ImaginPart * c.getRealPart() - this.RealPart * c.getImaginPart()) / d);
		return result;
	}
	//将当前的复数对象以字符串的形式表示
	public String toString(){
		return ""+this.RealPart+"+"+this.ImaginPart+"i";
	}

	public ComplexNumber exp()
	{
		return new ComplexNumber(Math.exp(RealPart) * Math.cos(ImaginPart), Math.exp(RealPart) * Math.sin(ImaginPart));
	}
}