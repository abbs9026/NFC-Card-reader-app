package com.freakyaxel.emvreader

import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.freakyaxel.emvparser.api.CardData
import com.freakyaxel.emvparser.api.CardDataResponse
import com.freakyaxel.emvparser.api.EMVReader
import com.freakyaxel.emvparser.api.EMVReaderLogger
import com.freakyaxel.emvparser.api.fold
import com.freakyaxel.emvreader.ui.theme.EMVReaderTheme
import android.widget.EditText
import android.widget.TextView
import android.text.TextWatcher
import android.text.Editable


class MainActivity : ComponentActivity(), NfcAdapter.ReaderCallback, EMVReaderLogger {

    private var amount: Int = 0
    private val cardStateLabel = mutableStateOf("Tap Card to read")
    private val emvReader = EMVReader.get(this)
    private var nfcAdapter: NfcAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        setContent {
            EMVReaderTheme {
                CardDataScreen(
                    data = cardStateLabel.value,
                    amount = amount,
                    onAmountEntered = { enteredAmount ->
                        amount = enteredAmount
                        startCardReading()
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        startCardReading()
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableReaderMode(this)
    }

    private fun startCardReading() {
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (nfcAdapter != null) {
            nfcAdapter?.enableReaderMode(
                this,
                this,
                NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK or
                        NfcAdapter.FLAG_READER_NFC_A or
                        NfcAdapter.FLAG_READER_NFC_B or
                        NfcAdapter.FLAG_READER_NFC_BARCODE or
                        NfcAdapter.FLAG_READER_NFC_F or
                        NfcAdapter.FLAG_READER_NFC_V,
                null
            )
        } else {
            cardStateLabel.value = "NFC not available"
        }
    }

    override fun emvLog(key: String, value: String) {
        Log.e(key, value)
    }

    override fun onTagDiscovered(tag: Tag) {
        cardStateLabel.value = "Reading Card..."

        val cardTag = EmvCardTag.get(tag)
        val cardData = emvReader.getCardData(cardTag)

        val isPinRequired = amount > 50
        val isTransactionBlocked = amount > 100

        if (isTransactionBlocked) {
            cardStateLabel.value = "Transaction Blocked. Amount exceeds the limit."
            return
        }

        cardStateLabel.value = cardData.fold(
            onError = { it.error.message },
            onSuccess = { getCardLabel(it.cardData) },
            onTagLost = { "Card lost. Keep card steady!" },
            onCardNotSupported = { getCardNotSupportedLabel(it) }
        )

        if (isPinRequired) {
            // Code to prompt for PIN entry
        }
    }

    private fun getCardNotSupportedLabel(response: CardDataResponse.CardNotSupported): String {
        val aids = response.aids
        return """
            Card is not supported!
            AID: ${aids.takeIf { it.isNotEmpty() }?.joinToString(" | ") ?: "NOT FOUND"}
        """.trimIndent()
    }

    private fun getCardLabel(cardData: CardData?): String {
        return """
            AID: ${cardData?.aid?.joinToString(" | ")}
            Number: ${cardData?.formattedNumber}
            Expires: ${cardData?.formattedExpDate}
        """.trimIndent()
    }
}

@Composable
fun CardDataScreen(
    data: String,
    amount: Int = 0, // Provide a default value for amount
    onAmountEntered: (Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = data)
        VerticalSpacer(height = 16.dp)

        AndroidView(
            factory = { context ->
                EditText(context).apply {
                    setText(amount.toString())
                }
            },
            update = { editText ->
                // No need for the addTextChangedListener here
            }
        )

        VerticalSpacer(height = 16.dp)

        Button(
            onClick = { onAmountEntered(amount) },
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Text("Process Transaction")
        }
    }
}

@Composable
fun VerticalSpacer(height: Dp) {
    Spacer(modifier = Modifier.height(height))
}

// Placeholder function for getCardLabel
private fun getCardLabel(cardData: CardData?): String {
    return "Card Label"
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    EMVReaderTheme {
        CardDataScreen(data = getCardLabel(null)) { }
    }
}