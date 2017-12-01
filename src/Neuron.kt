
/*
    TBA for actual function
 */

class Neuron() {
    fun how_bad_is_the_error(total:Int,count:Int):String{
        val percent=((count.toDouble()/total.toDouble())*100).toInt()
        when(percent){
            in 0..25 -> return "OH DEAR GOD THIS IS AWFUL"
            in 25..50 -> return "MEH, NOT BRILLIANT"
            in 50..75 -> return "PRETTY GOOD DATA THERE"
            in 75..100 -> return "I AM HAPPY WITH THIS"
            else -> return "WHAT THE HELL, I GOT A NEGATIVE PERCENTAGE, IS RGB OK?"
        }
    }
}