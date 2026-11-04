package com.example.petapp.ui

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation


class CpfVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = text.text.take(11)
        val out = buildString { for (i in trimmed.indices) { if (i == 3 || i == 6) append("."); if (i == 9) append("-"); append(trimmed[i]) } }
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int { if (offset <= 3) return offset.coerceAtMost(out.length); if (offset <= 6) return (offset + 1).coerceAtMost(out.length); if (offset <= 9) return (offset + 2).coerceAtMost(out.length); return (offset + 3).coerceAtMost(out.length) }
            override fun transformedToOriginal(offset: Int): Int { if (offset <= 3) return offset.coerceIn(0, trimmed.length); if (offset <= 7) return (offset - 1).coerceIn(0, trimmed.length); if (offset <= 11) return (offset - 2).coerceIn(0, trimmed.length); return (offset - 3).coerceIn(0, trimmed.length) }
        }
        return TransformedText(AnnotatedString(out), offsetMapping)
    }
}

class TelefoneVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = text.text.take(11)
        val out = buildString { for (i in trimmed.indices) { if (i == 0) append("("); if (i == 2) append(") "); if (i == 7) append("-"); append(trimmed[i]) } }
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int { if (offset <= 0) return 0; if (offset <= 2) return (offset + 1).coerceAtMost(out.length); if (offset <= 7) return (offset + 3).coerceAtMost(out.length); return (offset + 4).coerceAtMost(out.length) }
            override fun transformedToOriginal(offset: Int): Int { if (offset <= 1) return 0; if (offset <= 3) return (offset - 1).coerceIn(0, trimmed.length); if (offset <= 5) return 2.coerceAtMost(trimmed.length); if (offset <= 10) return (offset - 3).coerceIn(0, trimmed.length); if (offset == 11) return 7.coerceAtMost(trimmed.length); return (offset - 4).coerceIn(0, trimmed.length) }
        }
        return TransformedText(AnnotatedString(out), offsetMapping)
    }
}

class CepVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = text.text.take(8)
        val out = buildString { for (i in trimmed.indices) { if (i == 5) append("-"); append(trimmed[i]) } }
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int = (if (offset <= 5) offset else offset + 1).coerceAtMost(out.length)
            override fun transformedToOriginal(offset: Int): Int = (if (offset <= 6) offset else offset - 1).coerceAtMost(trimmed.length)
        }
        return TransformedText(AnnotatedString(out), offsetMapping)
    }
}

class DataVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = text.text.take(8)
        val out = buildString { for (i in trimmed.indices) { if (i == 2 || i == 4) append("/"); append(trimmed[i]) } }
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int { if (offset <= 2) return offset.coerceAtMost(out.length); if (offset <= 4) return (offset + 1).coerceAtMost(out.length); return (offset + 2).coerceAtMost(out.length) }
            override fun transformedToOriginal(offset: Int): Int { if (offset <= 3) return offset.coerceIn(0, trimmed.length); if (offset <= 6) return (offset - 1).coerceIn(0, trimmed.length); return (offset - 2).coerceIn(0, trimmed.length) }
        }
        return TransformedText(AnnotatedString(out), offsetMapping)
    }
}

class HoraVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = text.text.take(4)
        val out = buildString { for (i in trimmed.indices) { if (i == 2) append(":"); append(trimmed[i]) } }
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int { if (offset <= 2) return offset.coerceAtMost(out.length); return (offset + 1).coerceAtMost(out.length) }
            override fun transformedToOriginal(offset: Int): Int { if (offset <= 3) return offset.coerceIn(0, trimmed.length); return (offset - 1).coerceIn(0, trimmed.length) }
        }
        return TransformedText(AnnotatedString(out), offsetMapping)
    }
}
