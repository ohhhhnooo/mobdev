package dontdoitno.calculator

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private lateinit var resultText: TextView
    private lateinit var mathOperationText: TextView

    private var currentInput = ""
    private var firstNumber: Double = 0.0
    private var operator: String? = null
    private var expression = ""

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        resultText = findViewById(R.id.result)
        mathOperationText = findViewById(R.id.mathOperation)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        // Восстановление состояния
        if (savedInstanceState != null) {
            currentInput = savedInstanceState.getString("input", "")
            firstNumber = savedInstanceState.getDouble("first", 0.0)
            operator = savedInstanceState.getString("operator")
            expression = savedInstanceState.getString("expression", "")

            resultText.text = currentInput.ifEmpty { "0" }
            mathOperationText.text = expression
        }

        // Установка кнопок
        setupButtons()
    }

    private fun setupButtons() {
        // Обработка кнопок цифр
        val numberButtons = listOf(
            R.id.btn0, R.id.btn1, R.id.btn2,
            R.id.btn3, R.id.btn4, R.id.btn5,
            R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9
        )

        for (id in numberButtons) {
            findViewById<Button>(id).setOnClickListener {
                onNumberClick((it as Button).text.toString())
            }
        }

        // Обработка кнопок операторов
        findViewById<Button>(R.id.btnDivide).setOnClickListener {
            onOperatorClick("/")
        }

        findViewById<Button>(R.id.btnMultiply).setOnClickListener {
            onOperatorClick("*")
        }

        findViewById<Button>(R.id.btnPlus).setOnClickListener {
            onOperatorClick("+")
        }

        findViewById<Button>(R.id.btnMinus).setOnClickListener {
            onOperatorClick("-")
        }

        findViewById<Button>(R.id.btnPercent).setOnClickListener {
            onOperatorClick("%")
        }

        // Обработка кнопки точка
        findViewById<Button>(R.id.btnDot).setOnClickListener {
            onDotClick()
        }

        // Обработка кнопки убрать последний символ
        findViewById<Button>(R.id.btnDelSymbol).setOnClickListener {
            onDelSymbolClick()
        }

        // Обработка кнопки C (стереть)
        findViewById<Button>(R.id.btnClear).setOnClickListener {
            onClearClick()
        }

        // Обработка кнопки равно
        findViewById<Button>(R.id.btnEquals).setOnClickListener {
            onEqualsClick()
        }
    }

    //    UI
    private fun updateDisplay() {
        resultText.text = currentInput.ifEmpty { "0" }
    }

    // Логика
    private fun onNumberClick(number: String) {
        currentInput += number
        updateDisplay()
    }

    private fun onOperatorClick(op: String) {
        if (currentInput.isEmpty()) return

        firstNumber = currentInput.toDoubleOrNull() ?: return
        operator = op

        expression = "$currentInput $op"
        mathOperationText.text = expression

        currentInput = ""
    }

    private fun onDotClick() {
        if (!currentInput.contains(".")) {
            currentInput = if (currentInput.isEmpty()) "0." else "$currentInput."
            updateDisplay()
        }
    }

    private fun onDelSymbolClick() {
        if (currentInput.isNotEmpty()) {
            currentInput = currentInput.dropLast(1)
            updateDisplay()
        }
    }

    private fun onClearClick() {
        currentInput = ""
        firstNumber = 0.0
        operator = null
        expression = ""
        updateDisplay()
    }

    @SuppressLint("SetTextI18n")
    private fun onEqualsClick() {
        if (currentInput.isEmpty() || operator == null) return

        val secondNumber = currentInput.toDoubleOrNull() ?: return

        val result = when (operator) {
            "+" -> firstNumber + secondNumber
            "-" -> firstNumber - secondNumber
            "*" -> firstNumber * secondNumber
            "/" -> {
                if (secondNumber == 0.0) {
                    resultText.text = "Error"
                    return
                }
                firstNumber / secondNumber
            }
            else -> return
        }

        // показываем полное выражение
        mathOperationText.text = "$firstNumber $operator $secondNumber"

        currentInput = result.toString()
        operator = null
        expression = ""

        updateDisplay()
    }

    // сохранения состояния
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("input", currentInput)
        outState.putDouble("first", firstNumber)
        outState.putString("operator", operator)
        outState.putString("expression", expression)
    }



//    1. Обработка всех кнопок -- DONE
//    2. Функция записи чисел в math_operation
//    3. Сохранение состояния
//    4. Обработка ошибок (деление на ноль)
//    5. Функции выполнения математических операций
//    6.
}