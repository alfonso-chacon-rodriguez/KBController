// See LICENSE for license details.

package work

//Nos basaremos en el Verilog del Pong Chu, pp. 235-237
//Luego le annadiremos las sennales necesarias para probarlo con un Testbench en C++ mas avanzado
import chisel3._

class ps2_rx extends Module {
  val io = IO(new Bundle {
    val ps2d  = Input(Bool()) //Linea de datos del PS/2
    val ps2c  = Input(Bool()) //Linea de reloj del PS/2
    val output  = Output(UInt(8.W))
    val rx_done_tick  = Output(Bool())
  })
// ------------------------------------------------------------------
// -------- Definimos la FSM de control-----------------------------
	val sIdle :: sDps :: sLoad :: Nil = Enum(3) //Definimos tres estados
  	val state = Reg(init = sIdle) //Registro de estado, que inicia en el primer estado
	val shift = Bool() //Se define variable si hay que desplazar


  switch (state) {
    is (sIdle) {
      when (fall_edge) { state := sDps
			 } //Si detectamos un flanco de caida, arrancamos
    }
    is (sDps) { //tenemos que contar ocho bits de entrada, mas un  bit paridad, un bit de parada
      when (fall_edge) {
	when (n_reg === 0) {state := sLoad 
			    } // Mientras no hayan llegado los 10 bits, seguimos metiendo datos
    }
    is (sLoad) { //Esto mete un clock extra para completar el ultimo desplazamiento
 	state := sIdle
	shift :=1
    }
  } //Se termina la logica de siguiente estado de la FSM

//Sennales de control de la FSM
// Cuando terminamos se genera el rx_done_tick
	when (state === sLoad) {io.rx_done_tick :=1}
	.otherwise {io.rx_done_tick :=0}
//Si estamos en sDps o sLoad debemos desplazar
	when ((state === sLoad) || (state === sDps)  ) {shift :=1}
	.otherwise {shift :=0}

// ------------------ Termina FSM ----------------------------

// Registro de desplazamiento para ir guardando los datos, trataremos de usar la biblioteca de Chisel

	val b_reg := ShiftRegister(io.ps2d, init= UInt(0, widht=1), 11, shift) //Si hay flaco valido de caida, se desplaza un dato
	//Aun no sabemos como se inicializa el registro!
	//SI esto no fnciona usamos la definicion manual Ver Pagina 32 del chisel-bootcamp

// Debemos capturar el flanco del reloj, filtrando el ruido


//Se debe contar los bits despues de un start bit valido 

  when (io.e) { x := io.a; y := io.b }
  io.z := x
  io.v := y === 0.U
}
