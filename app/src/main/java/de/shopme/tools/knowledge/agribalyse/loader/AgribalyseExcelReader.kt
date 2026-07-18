package de.shopme.tools.knowledge.agribalyse.loader

import de.shopme.tools.knowledge.agribalyse.classifier.AgribalyseSheetClassifier
import de.shopme.tools.knowledge.agribalyse.mapper.AgribalyseHeaderNormalizer
import de.shopme.tools.knowledge.agribalyse.model.AgribalyseSheetInfo
import de.shopme.tools.knowledge.agribalyse.model.AgribalyseSheetLayout
import de.shopme.tools.knowledge.agribalyse.model.AgribalyseSheetType
import de.shopme.tools.knowledge.agribalyse.parser.AgribalyseSheetPreviewHandler
import de.shopme.tools.knowledge.data.KnowledgeDataDirectories
import org.apache.poi.openxml4j.opc.OPCPackage
import org.apache.poi.xssf.eventusermodel.XSSFReader
import java.io.File

class AgribalyseExcelReader {

    fun sheetNames(
        file: File = defaultAgribalyseFile()
    ): List<String> {
        requireValidAgribalyseFile(file)

        OPCPackage.open(file).use { packageFile ->
            val reader = XSSFReader(packageFile)

            val names = mutableListOf<String>()

            val sheetIterator = reader.sheetsData as XSSFReader.SheetIterator

            while (sheetIterator.hasNext()) {
                sheetIterator.next().use {
                    names += sheetIterator.sheetName
                }
            }

            return names
        }
    }

    fun sheets(
        file: File = defaultAgribalyseFile()
    ): List<AgribalyseSheetInfo> {

        val classifier = AgribalyseSheetClassifier()

        return sheetNames(file)
            .mapIndexed { index, name ->
                AgribalyseSheetInfo(
                    index = index,
                    name = name,
                    type = classifier.classify(name)
                )
            }
    }

    fun previewRows(
        sheetType: AgribalyseSheetType,
        maxRows: Int,
        file: File = defaultAgribalyseFile()
    ): List<List<String>> {
        require(maxRows > 0) {
            "maxRows must be > 0"
        }

        val targetSheet = sheets(file)
            .single { it.type == sheetType }

        return readRows(
            file = file,
            sheetIndex = targetSheet.index,
            maxRows = maxRows
        )
    }

    fun readHeader(
        sheetType: AgribalyseSheetType,
        layout: AgribalyseSheetLayout,
        file: File = defaultAgribalyseFile()
    ): List<String> {

        val rows = previewRows(
            sheetType = sheetType,
            maxRows = layout.headerRowIndex + 1,
            file = file
        )

        require(rows.size > layout.headerRowIndex) {
            "Header row ${layout.headerRowIndex} not found."
        }

        return rows[layout.headerRowIndex]
    }

    fun readRecords(
        sheetType: AgribalyseSheetType,
        layout: AgribalyseSheetLayout,
        maxRecords: Int? = null,
        file: File = defaultAgribalyseFile()
    ): List<Map<String, String>> {
        require(maxRecords == null || maxRecords > 0) {
            "maxRecords must be null or > 0"
        }

        val rows =
            if (maxRecords == null) {
                previewRows(
                    sheetType = sheetType,
                    maxRows = Int.MAX_VALUE,
                    file = file
                )
            } else {
                previewRows(
                    sheetType = sheetType,
                    maxRows = layout.firstDataRowIndex + maxRecords,
                    file = file
                )
            }

        require(rows.size > layout.headerRowIndex) {
            "Header row ${layout.headerRowIndex} not found."
        }

        val categoryHeader = rows[layout.headerRowIndex - 1]
        val fieldHeader = rows[layout.headerRowIndex]

        val header = AgribalyseHeaderNormalizer().normalize(
            categoryHeader = categoryHeader,
            fieldHeader = fieldHeader
        )

        val dataRows =
            rows.drop(layout.firstDataRowIndex)

        return if (maxRecords == null) {
            dataRows
        } else {
            dataRows.take(maxRecords)
        }.map { row ->
            header.mapIndexedNotNull { index, columnName ->
                if (columnName.isBlank()) {
                    null
                } else {
                    columnName to row.getOrElse(index) { "" }.trim()
                }
            }.toMap()
        }
    }

    private fun readRows(
        file: File,
        sheetIndex: Int,
        maxRows: Int
    ): List<List<String>> {
        val rows = mutableListOf<List<String>>()

        OPCPackage.open(file).use { packageFile ->
            val reader = XSSFReader(packageFile)
            val sheetIterator = reader.sheetsData as XSSFReader.SheetIterator

            var currentIndex = 0

            while (sheetIterator.hasNext()) {
                val stream = sheetIterator.next()

                val sharedStrings = reader.sharedStringsTable

                stream.use {
                    if (currentIndex == sheetIndex) {
                        val handler = AgribalyseSheetPreviewHandler(
                            sharedStrings = sharedStrings,
                            maxRows = maxRows,
                            onRow = { row ->
                                rows += row
                            }
                        )

                        val parser = javax.xml.parsers.SAXParserFactory
                            .newInstance()
                            .newSAXParser()
                            .xmlReader

                        parser.contentHandler = handler
                        parser.parse(org.xml.sax.InputSource(it))

                        return rows
                    }
                }

                currentIndex++
            }
        }

        error("Agribalyse sheet index not found: $sheetIndex")
    }

    fun defaultAgribalyseFile(): File {
        val directory = KnowledgeDataDirectories.agribalyseRaw

        require(directory.exists()) {
            "Agribalyse raw directory not found: ${directory.absolutePath}"
        }

        require(directory.isDirectory) {
            "Agribalyse raw path is not a directory: ${directory.absolutePath}"
        }

        val matchingFiles = directory
            .listFiles()
            .orEmpty()
            .filter {
                it.isFile &&
                        it.extension.equals("xlsx", ignoreCase = true) &&
                        it.name.startsWith("AGRIBALYSE", ignoreCase = true)
            }
            .sortedBy { it.name }

        require(matchingFiles.isNotEmpty()) {
            "No Agribalyse Excel file found in ${directory.absolutePath}"
        }

        require(matchingFiles.size == 1) {
            "Multiple Agribalyse Excel files found in ${directory.absolutePath}: ${
                matchingFiles.map { it.name }
            }"
        }

        return matchingFiles.single()
    }

    private fun requireValidAgribalyseFile(file: File) {
        require(file.exists()) {
            "Agribalyse Excel file not found: ${file.absolutePath}"
        }

        require(file.isFile) {
            "Agribalyse path is not a file: ${file.absolutePath}"
        }

        require(file.extension.equals("xlsx", ignoreCase = true)) {
            "Agribalyse file must be an .xlsx file: ${file.absolutePath}"
        }
    }
}