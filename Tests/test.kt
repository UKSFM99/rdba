import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class test_latlng{
    @Test
    fun latitude_integrity() {
        assertEquals(0.0, LatLng(0.0, 0.0).get_lat())
    }
    @Test
    fun lonitude_integrity() {
        assertEquals(0.0, LatLng(0.0, 0.0).get_long())
    }
    @Test
    fun latlng_to_arraylist(){
        assertEquals(arrayListOf(0.0,0.0),LatLng(0.0,0.0).toArrayList())
    }
    @Test
    fun neuron_sigmoid(){
        var i=-100.0
        while( i <=10.0){
            assertTrue(Neuron().sigmoid(i)>0.0)
            i+=0.1
        }
    }
    /*
    @Test
    fun heading(){
        println(Neuron("1","1").get_bearing_degrees(
                LatLng(51.422809, -0.863938),
                LatLng(51.425634, -0.871135)
        ))
    }
    */
}