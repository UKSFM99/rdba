
/*
    TBA for actual function
 */

class Neuron() {
    fun how_bad_is_the_error(total:Int,count:Int):String{
        return when(((count.toDouble()/total.toDouble())*100).toInt()){
            in 0..25 -> "OH DEAR GOD THIS IS AWFUL"
            in 25..50 -> "MEH, NOT BRILLIANT"
            in 50..75 -> "PRETTY GOOD DATA THERE"
            in 75..100 -> "I AM HAPPY WITH THIS"
            else -> "WHAT THE HELL, I GOT A NEGATIVE PERCENTAGE, IS RGB OK?"
        }
    }
}